package com.coolingye.playerlive.srs


import com.alibaba.fastjson.annotation.JSONField
import org.webrtc.PeerConnection

data class SrsRequestBody(
    /**
     * [PeerConnection.createOffer]返回的sdp
     */
    @JSONField(name = "sdp")
    var sdp: String? = null,
    /**
     * 拉取的WebRTC流地址
     */
    @JSONField(name = "streamurl")
    var streamurl: String? = null,

    @JSONField(name = "api")
    var api: String? = null,

    @JSONField(name = "clientip")
    var clientip: String? = null
)