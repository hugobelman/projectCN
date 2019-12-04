package com.computoenlanube.nube.models

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface CloudService {
    @POST("/log")
    fun login(@Body user: User): Call<LogResponse>

    @POST("/logout")
    fun logout(@Header("Cookie") cookie: String): Call<LogResponse>

    @POST("/add")
    fun signUp(@Body user: User): Call<AddResponse>

    @GET("/datos")
    fun getAllFiles(@Header("Cookie") cookie: String): Call<List<MetadataFile>>

    @Multipart
    @POST("/subir")
    fun uploadFile(@Header("Cookie") cookie: String, @Part file: MultipartBody.Part): Call<UploadResponse>

    @DELETE("/del/{id}")
    fun deleteFile(@Header("Cookie") cookie: String, @Path("id") fileId: Int): Call<DeleteResponse>

    @PUT("/actualizar")
    fun updatePass(@Header("Cookie") cookie: String, @Body newPass: NewPassBody): Call<NewPassResponse>
}