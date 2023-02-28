package com.coolingye.playerlive

import android.content.Context
import android.text.TextUtils
import com.coolingye.playerlive.Constant.SRS_SERVER_IP
import com.coolingye.playerlive.service.RestfulCallback
import com.coolingye.playerlive.service.RetrofitClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.webrtc.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException


class WebRTCClient(
    private val context: Context,
    private val localPlayerView: SurfaceViewRenderer,
    private val netWorkPlayerView: SurfaceViewRenderer,
    private val isPush: Boolean = false
) : SdpObserver {
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
        rtcConfig.apply {
            enableCpuOveruseDetection = false
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        /*
         <p>For users who wish to send multiple audio/video streams and need to stay interoperable with legacy WebRTC implementations, specify PLAN_B.
         <p>For users who wish to send multiple audio/video streams and/or wish to use the new RtpTransceiver API, specify UNIFIED_PLAN.
         */
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        netWorkPlayerView.init(eglBaseContext, null)


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

                override fun onIceCandidate(iceCandidate: IceCandidate?) {
                    super.onIceCandidate(iceCandidate)
                    peerConnection.addIceCandidate(iceCandidate)
                }

                override fun onIceCandidatesRemoved(array: Array<out IceCandidate>?) {
                    super.onIceCandidatesRemoved(array)
                    peerConnection.removeIceCandidates(array)
                }

            })?.apply {
            if (isPush) {
                addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
                )
                addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
                )
            } else {
                addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                )
                addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
                )
            }
        }!!


        if (isPush) {
            getVideoTrack().setEnabled(true)
            getAudioTrack().setEnabled(true)
            peerConnection.addTrack(getVideoTrack())
            peerConnection.addTrack(getAudioTrack())
        }

        localPlayerView.setMirror(true)
        localPlayerView.init(eglBaseContext, null)
        getVideoTrack().addSink(localPlayerView)


        peerConnection.createOffer(this, MediaConstraints())
    }

    private val pullUrl = "webrtc://%s/live/livestream"

    private fun connectRTC(desc: String) {
        val streamUrl: String = String.format(pullUrl, loadAddress)
        val req = SrsRequestBody(desc, streamUrl)
        if (isPush) {
            RetrofitClient.mService.publish(req).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : RestfulCallback<SrsResponsBody>() {
                    override fun onSuccess(t: SrsResponsBody) {
                        if (t.code == 0) {
                            t.sdp?.let { setRemoteSdp(it) }
                        }
                    }

                    override fun onFailure(e: Throwable?) {

                    }

                })
        } else {
            RetrofitClient.mService.play(req).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : RestfulCallback<SrsResponsBody>() {
                    override fun onSuccess(t: SrsResponsBody) {
                        if (t.code == 0) {
                            t.sdp?.let { setRemoteSdp(it) }
                        }
                    }

                    override fun onFailure(e: Throwable?) {

                    }

                })
        }

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

    private fun getVideoTrack(): VideoTrack {
        videoCapture = createVideoCapture()!!
        videoSource = peerConnectionFactory.createVideoSource(videoCapture.isScreencast)
        surfaceTextureHelper = SurfaceTextureHelper.create("surface_texture_thread", eglBaseContext)
        videoCapture.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
        videoCapture.startCapture(640, 480, 30)
        videoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource)
        return videoTrack
    }

    private fun getAudioTrack(): AudioTrack {
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