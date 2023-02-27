package com.coolingye.playerlive

import android.content.Context
import android.text.TextUtils
import com.coolingye.playerlive.Constant.SRS_SERVER_IP
import com.coolingye.playerlive.service.RestfulCallback
import com.coolingye.playerlive.service.RetrofitClient
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.webrtc.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException


class WebRTCClient(private val context: Context, private val localPlayerView: SurfaceViewRenderer, private val netWorkPlayerView: SurfaceViewRenderer ) : SdpObserver {
    private lateinit var peerConnection: PeerConnection
    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private lateinit var audioSource: AudioSource
    private lateinit var videoSource: VideoSource
    private lateinit var audioTrack: AudioTrack
    private lateinit var videoTrack: VideoTrack

    private lateinit var videoCapture: VideoCapturer
    private lateinit var surfaceTextureHelper: SurfaceTextureHelper
    private val eglBaseContext = EglBase.create().eglBaseContext

    private val loadAddress: String = SRS_SERVER_IP

    companion object {
        const val VIDEO_TRACK_ID = "ARDAMSv0"
        const val AUDIO_TRACK_ID = "ARDAMSa0"
    }

    fun execute() {
        initPeer()
        initRTC()
    }

    private fun initPeer() {
        val options = PeerConnectionFactory.Options()
        val encoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
    }

    private fun initRTC() {
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        /*
         <p>For users who wish to send multiple audio/video streams and need to stay interoperable with legacy WebRTC implementations, specify PLAN_B.
         <p>For users who wish to send multiple audio/video streams and/or wish to use the new RtpTransceiver API, specify UNIFIED_PLAN.
         */
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        peerConnection = peerConnectionFactory.createPeerConnection(
            rtcConfig,
            object : PeerConnectionObserver() {
                override fun onAddStream(mediaStream: MediaStream?) {
                    super.onAddStream(mediaStream)
                    mediaStream?.let {
                        if (it.videoTracks.isNotEmpty()) {
                            it.videoTracks.first().addSink(netWorkPlayerView)
                        }
                    }
                }
            })?.apply {
            addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
            addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
        }!!

//        audioTrack.setEnabled(true)
//        videoTrack.setEnabled(true)

        peerConnection.addTrack(getVideoTrack())
        peerConnection.addTrack(getAudioTrack())

        localPlayerView.setMirror(true)
        localPlayerView.init(eglBaseContext, null)
        getVideoTrack().addSink(localPlayerView)

        peerConnection.createOffer(this, MediaConstraints())
    }

    private val pushUrl = "webrtc://%s/live/livestream"

    private fun connectRTC(desc: String) {
        val streamUrl: String = String.format(pushUrl, loadAddress)
        val req = SrsRequestBody(desc, streamUrl, "http://192.168.2.86:1985/rtc/v1/play/", "192.168.30.183")
        println("pull-json:${Gson().toJson(req)}")
        RetrofitClient.mService.play(req).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(object : RestfulCallback<SrsResponsBody>(){
                override fun onSuccess(t: SrsResponsBody) {
                    if (req.sdp?.isNotEmpty() == true) {
                        setRemoteSdp(req.sdp)
                    }
                }

                override fun onFailure(e: Throwable?) {
                    e
                }

            })

    }

    fun getIpAddressString(): String? {
        try {
            val enNetI = NetworkInterface
                .getNetworkInterfaces()
            while (enNetI.hasMoreElements()) {
                val netI = enNetI.nextElement()
                val enumIpAddr = netI
                    .inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (inetAddress is Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
        return "0.0.0.0"
    }

    private fun setRemoteSdp(sdp: String) {
        peerConnection.let {
            val remoteSdp = SessionDescription(SessionDescription.Type.ANSWER, sdp)
            peerConnection.setRemoteDescription(this, remoteSdp)
        }
    }

    private fun getVideoTrack() : VideoTrack {
        videoCapture = createVideoCapture()!!
        videoSource = peerConnectionFactory.createVideoSource(videoCapture.isScreencast)
        surfaceTextureHelper = SurfaceTextureHelper.create("surface_texture_thread", eglBaseContext)
        videoCapture.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
        videoCapture.startCapture(640, 480, 30)
        videoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource)
        return videoTrack
    }

    private fun getAudioTrack() : AudioTrack {
        audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        audioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource)
        return audioTrack
    }

    private fun createVideoCapture(): VideoCapturer? {
        return if (Camera2Enumerator.isSupported(context)) {
            createCameraCapture(Camera2Enumerator(context))
        } else {
            createCameraCapture(Camera1Enumerator(true))
        }
    }

    private fun createCameraCapture(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapture: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapture != null) {
                    return videoCapture
                }
            }
        }

        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapture: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapture != null) {
                    return videoCapture
                }
            }
        }
        return null
    }

    override fun onCreateSuccess(p0: SessionDescription?) {
        p0?.let {
            if (it.type == SessionDescription.Type.OFFER) {

                //设置setLocalDescription offer返回sdp
                peerConnection.setLocalDescription(this, it)
                if (!TextUtils.isEmpty(it.description)) {
                    connectRTC(it.description)
                }
            }
        }
    }

    override fun onSetSuccess() {

    }

    override fun onCreateFailure(p0: String?) {

    }

    override fun onSetFailure(p0: String?) {

    }

    fun destroy() {
        videoCapture.dispose()
        surfaceTextureHelper.dispose()
        peerConnection.dispose()
        peerConnectionFactory.dispose()
    }
}