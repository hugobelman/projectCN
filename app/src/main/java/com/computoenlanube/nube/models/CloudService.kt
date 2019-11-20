package com.computoenlanube.nube.models

import retrofit2.Call
import retrofit2.http.*

interface CloudService {
    @POST("/log")
    fun login(@Body user: User): Call<LogResponse>

    @POST("/add")
    fun signUp(@Body user: User): Call<AddResponse>

    @GET("/datos")
    fun getAllFiles(@Header("Cookie") cookie: String): Call<List<MetadataFile>>
}