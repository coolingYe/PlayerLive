package com.coolingye.playerlive.service

object Constant {
    /**
     * SRS服务器IP
     */
    const val SRS_SERVER_IP = "192.168.2.244"

    /**
     * SRS服务http请求端口，1985
     */
    private const val SRS_SERVER_HTTP_PORT = "1985"

    const val SRS_SERVER_HTTP = "$SRS_SERVER_IP:$SRS_SERVER_HTTP_PORT"

}
