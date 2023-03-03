package com.coolingye.playerlive.srs


import io.reactivex.Observable

import retrofit2.http.Body
import retrofit2.http.POST

/**
 *
 * @author  ShenBen
 * @date    2021/8/19 12:38
 * @email   714081644@qq.com
 */
interface SrsApiService {

    @POST("/rtc/v1/play/")
    fun play(@Body body: SrsRequestBody): Observable<SrsResponsBody>

    @POST("/rtc/v1/publish/")
    fun publish(@Body body: SrsRequestBody): Observable<SrsResponsBody>
}