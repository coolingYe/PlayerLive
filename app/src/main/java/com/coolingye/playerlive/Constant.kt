package com.coolingye.playerlive

object Constant {
    /**
     * SRS服务器IP
     */
    const val SRS_SERVER_IP = "192.168.2.86"

    /**
     * SRS服务http请求端口，8080
     */
    private const val SRS_SERVER_HTTP_PORT = "1985"

    const val SRS_SERVER_HTTP = "$SRS_SERVER_IP:$SRS_SERVER_HTTP_PORT"

}
