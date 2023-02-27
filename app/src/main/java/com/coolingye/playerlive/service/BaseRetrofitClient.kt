package com.coolingye.playerlive.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit

abstract class BaseRetrofitClient {

    abstract fun generateOkHttpBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder

    abstract fun generateRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder

    fun <T> getApiService(service: Class<T>, baseUrl: String): T {
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(generateOkHttpBuilder(OkHttpClient.Builder()).build())
        return generateRetrofitBuilder(retrofitBuilder)
            .build().create(service)
    }
}
