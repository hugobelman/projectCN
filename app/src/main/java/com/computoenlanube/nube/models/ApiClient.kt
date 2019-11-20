package com.computoenlanube.nube.models

import android.app.Activity
import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

const val BASE_URL = "https://backcloud2019.herokuapp.com/"

object ApiClient {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    private val client = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val cloudService = client.create(CloudService::class.java)

    private fun getSharedPreferences(activity: Activity) = activity.getSharedPreferences("com.computoenlanube.nube.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)

    fun getAuthCookie(activity: Activity): String? {
        val sharedPreferences = getSharedPreferences(activity)
        return sharedPreferences.getString("cookie", null)
    }

    fun setAuthCookie(activity: Activity, cookie: String) {
        val sharedPreferences = getSharedPreferences(activity)

        with(sharedPreferences.edit()) {
            putString("cookie", cookie)
            commit()
        }
    }

    fun deleteAuthCookie(activity: Activity) {
        val sharedPreferences = getSharedPreferences(activity)

        with(sharedPreferences.edit()) {
            putString("cookie", null)
            commit()
        }
    }
}