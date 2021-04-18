package com.example.kotlin_app.api

import com.example.kotlin_app.data.LoginReq
import com.example.kotlin_app.data.LoginRes
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body

interface MainAPI {
    @POST("api/login")
    fun login(@Body loginReq: LoginReq): Call<LoginRes>
}
