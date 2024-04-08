package com.example.mytravelapp.Server

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET



interface Caller {

    @GET("items/songs")
    suspend fun getMusicData(): Response<ResponseBody>

}

