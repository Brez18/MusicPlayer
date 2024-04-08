package com.example.mytravelapp.Server

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {

    private const val BASEURL = "https://cms.samespace.com/"
    private val okHttpClient = OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS)
                                .readTimeout(20, TimeUnit.SECONDS)
                                .build()

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(BASEURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}