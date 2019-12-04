package com.computoenlanube.nube.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.computoenlanube.nube.R
import com.computoenlanube.nube.models.ApiClient
import com.computoenlanube.nube.models.LogResponse
import com.computoenlanube.nube.models.NewPassBody
import com.computoenlanube.nube.models.NewPassResponse
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_change_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    private var loading = true
        set(value) {
            if (value) showLoading()
            else showIdle()
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val authCookie = ApiClient.getAuthCookie(this)!!

        changePass_btn.setOnClickListener {
            val oldPass = oldPass_til.editText?.text!!.toString()
            val newPass = newPass_til.editText?.text!!.toString()

            loading = true
            ApiClient.cloudService.updatePass(authCookie, NewPassBody(oldPass, newPass)).enqueue(object : Callback<NewPassResponse> {
                override fun onFailure(call: Call<NewPassResponse>, t: Throwable) {
                    loading =false
                    Toast.makeText(this@ChangePasswordActivity, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<NewPassResponse>, response: Response<NewPassResponse>) {
                    loading =false
                    val r = response.body()!!

                    if (!r.error) {
                        this@ChangePasswordActivity.setResult(Activity.RESULT_OK)
                        this@ChangePasswordActivity.finish()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, r.eror, Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun showLoading() {
        changePass_btn.isEnabled = false
    }

    private fun showIdle() {
        changePass_btn.isEnabled = true
    }
}
