package com.coolingye.playerlive.service

import android.util.Log
import com.coolingye.playerlive.service.Constant.SRS_SERVER_HTTP
import com.coolingye.playerlive.srs.SrsApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

abstract class RetrofitClient : BaseRetrofitClient() {

    companion object {
        const val BASE_URL = "http://$SRS_SERVER_HTTP"
    }

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor { Log.i("RetrofitLog", "Retrofit callback = $it") }.apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    override fun generateOkHttpBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return builder.apply {
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(60, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
            addInterceptor(loggingInterceptor)
        }
    }

    override fun generateRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.apply {
            addConverterFactory(GsonConverterFactory.create())
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        }
    }
}

object SrsRetrofit : RetrofitClient() {
    val mService by lazy {
        getApiService(
            SrsApiService::class.java,
            BASE_URL
        )
    }

}
