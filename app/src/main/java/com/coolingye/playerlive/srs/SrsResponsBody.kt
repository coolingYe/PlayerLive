package com.coolingye.playerlive.srs


import com.alibaba.fastjson.annotation.JSONField
import org.webrtc.PeerConnection

data class SrsResponsBody(
    /**
     * 0：成功
     */
    @JSONField(name = "code")
    val code: Int,
    /**
     * 用于设置[PeerConnection.setRemoteDescription]
     */
    @JSONField(name = "sdp") val sdp: String?,
    @JSONField(name = "server")
    val server: String?,
    @JSONField(name = "sessionid")
    val sessionId: String?
)