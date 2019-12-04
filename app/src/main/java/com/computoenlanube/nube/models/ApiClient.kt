package com.computoenlanube.nube.models

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    const val BASE_URL = "https://backcloud2019.herokuapp.com"

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

    fun showError(context: Context, httpcode: Int) {
        AlertDialog.Builder(context).apply {
            setTitle("Error al comunicarse con el servidor")
            setMessage("El servidor no está respondiendo como debería (HTTP code $httpcode)")
            setPositiveButton("Aceptar", null)
        }.show()
    }
}