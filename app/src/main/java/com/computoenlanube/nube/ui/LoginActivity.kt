package com.computoenlanube.nube.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.computoenlanube.nube.R
import com.computoenlanube.nube.models.AddResponse
import com.computoenlanube.nube.models.ApiClient
import com.computoenlanube.nube.models.LogResponse
import com.computoenlanube.nube.models.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progressBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class LoginActivity : AppCompatActivity() {

    private var loading = false
    set(value) {
        if (value) showLoading()
        else showIdle()
        field = value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        login_btn.setOnClickListener {
            val user = User(userName_til.editText!!.text.toString(), pass_til.editText!!.text.toString())

            loading = true
            ApiClient.cloudService.login(user).enqueue(object : Callback<LogResponse> {
                override fun onFailure(call: Call<LogResponse>, t: Throwable) {
                    loading = false
                    Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<LogResponse>, response: Response<LogResponse>) {
                    loading = false

                    val body = response.body()

                    when {
                        body?.log == null -> ApiClient.showError(this@LoginActivity,  response.code())

                        body.log -> {
                            ApiClient.setAuthCookie(this@LoginActivity, response.headers().get("Set-Cookie")!!)
                            startApp()
                        }

                        body.log == false -> Toast.makeText(this@LoginActivity, body.status, Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }

        signup_btn.setOnClickListener {
            val user = User(userName_til.editText!!.text.toString(), pass_til.editText!!.text.toString())

            loading = true
            ApiClient.cloudService.signUp(user).enqueue(object : Callback<AddResponse> {
                override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                    loading = false

                    Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                    loading = false

                    val body = response.body()

                    when {
                        body?.add == null -> ApiClient.showError(this@LoginActivity, response.code())

                        body.add -> {
                            ApiClient.setAuthCookie(this@LoginActivity, response.headers().get("Set-Cookie")!!)
                            startApp()
                        }

                        body.add == false -> Toast.makeText(this@LoginActivity, body.status, Toast.LENGTH_SHORT).show()
                    }
                }

            })
        }
    }

    private fun showLoading() {
        userName_til.editText!!.isEnabled = false
        pass_til.editText!!.isEnabled = false
        login_btn.visibility = View.INVISIBLE
        signup_btn.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    private fun showIdle() {
        userName_til.editText!!.isEnabled = true
        pass_til.editText!!.isEnabled = true
        login_btn.visibility = View.VISIBLE
        signup_btn.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
    }

    private fun startApp() {
        val intent = Intent(this@LoginActivity, FilesActivity::class.java)
        startActivity(intent)
        this@LoginActivity.finish()
    }
}
