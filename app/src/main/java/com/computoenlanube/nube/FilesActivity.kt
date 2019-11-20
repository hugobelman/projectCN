package com.computoenlanube.nube

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.computoenlanube.nube.models.ApiClient
import com.computoenlanube.nube.models.MetadataFile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)

        val authCookie = ApiClient.getAuthCookie(this)

        ApiClient.cloudService.getAllFiles(authCookie!!).enqueue(object : Callback<List<MetadataFile>> {
            override fun onFailure(call: Call<List<MetadataFile>>, t: Throwable) {
                Toast.makeText(this@FilesActivity, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<MetadataFile>>, response: Response<List<MetadataFile>>) {
                val files = response.body()

                files?.map {
                    Log.d("file", it.toString())
                }
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_files, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout_menuItem -> {
                ApiClient.deleteAuthCookie(this)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
