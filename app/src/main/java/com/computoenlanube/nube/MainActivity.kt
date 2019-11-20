package com.computoenlanube.nube

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.computoenlanube.nube.models.AddResponse
import com.computoenlanube.nube.models.ApiClient
import com.computoenlanube.nube.models.LogResponse
import com.computoenlanube.nube.models.User
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private var loading = false
    set(value) {
        if (value) showLoading()
        else showIdle()
        field = value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val authCookie = ApiClient.getAuthCookie(this)

        if (authCookie != null) startApp()

        login_btn.setOnClickListener {
            val user = User(userName_til.editText!!.text.toString(), pass_til.editText!!.text.toString())

            loading = true
            ApiClient.cloudService.login(user).enqueue(object : Callback<LogResponse> {
                override fun onFailure(call: Call<LogResponse>, t: Throwable) {
                    loading = false
                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<LogResponse>, response: Response<LogResponse>) {
                    loading = false

                    val body = response.body()!!

                    if (body.log) {
                        ApiClient.setAuthCookie(this@MainActivity, response.headers().get("Set-Cookie")!!)
                        startApp()
                    }
                    else Toast.makeText(this@MainActivity, body.status, Toast.LENGTH_SHORT).show()
                }

            })
        }

        signup_btn.setOnClickListener {
            val user = User(userName_til.editText!!.text.toString(), pass_til.editText!!.text.toString())

            loading = true
            ApiClient.cloudService.signUp(user).enqueue(object : Callback<AddResponse> {
                override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                    loading = false

                    Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                    loading = false

                    val body = response.body()!!

                    val userWasAdded = body.add ?: false

                    if (userWasAdded) {
                        ApiClient.setAuthCookie(this@MainActivity, response.headers().get("Set-Cookie")!!)
                        startApp()
                    }
                    else Toast.makeText(this@MainActivity, body.status, Toast.LENGTH_LONG).show()
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
        val intent = Intent(this@MainActivity, FilesActivity::class.java)
        startActivity(intent)
        this@MainActivity.finish()
    }
}
