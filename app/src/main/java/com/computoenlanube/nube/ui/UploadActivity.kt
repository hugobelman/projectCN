package com.computoenlanube.nube.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.computoenlanube.nube.R
import kotlinx.android.synthetic.main.activity_upload.*
import androidx.core.app.ActivityCompat.startActivityForResult
import android.content.Intent
import android.database.Cursor
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.core.net.toFile
import com.computoenlanube.nube.models.ApiClient
import com.computoenlanube.nube.models.UploadResponse
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_upload.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class UploadActivity : AppCompatActivity() {

    private val PICKFILE_REQUEST_CODE = 100
    private var uri: Uri? = null
    private var fileName: String? = null
    private var loading = false
        set(value) {
            if (value) showLoading()
            else showIdle()
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val authCookie = ApiClient.getAuthCookie(this)!!

        selectFile_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "*/*"
            startActivityForResult(intent, PICKFILE_REQUEST_CODE)
        }

        upload_btn.setOnClickListener {
            if (uri != null) {
                val bytes = contentResolver.openInputStream(uri!!)!!.readBytes()

                if (bytes.size > 1e+7) {
                    Snackbar.make(it, "Solo se puede subir archivos menores a 10 MB", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val requestBody = RequestBody.create(MediaType.parse(contentResolver.getType(uri!!)!!), bytes)
                val formDataFile = MultipartBody.Part.createFormData("file", fileName, requestBody)

                loading = true
                ApiClient.cloudService.uploadFile(authCookie, formDataFile).enqueue(object : Callback<UploadResponse>{
                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        Toast.makeText(this@UploadActivity, t.message, Toast.LENGTH_SHORT).show()
                        loading = false
                    }

                    override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                        loading = false
                        setResult(Activity.RESULT_OK)
                        this@UploadActivity.finish()
                    }
                })

            } else {
              Snackbar.make(it, "No se ha elegido un archivo", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK -> {
                uri = data!!.data

                val cursor: Cursor? = contentResolver.query(
                    uri!!, null, null, null, null, null
                )

                cursor?.use {
                    // moveToFirst() returns false if the cursor has 0 rows. Very handy for
                    // "if there's anything to look at, look at it" conditionals.
                    if (it.moveToFirst()) {

                        // Note it's called "Display Name". This is
                        // provider-specific, and might not necessarily be the file name.
                        fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }

                    fileName_et.setText(fileName)
                }
            }
        }

        return super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        selectFile_btn.isEnabled = false
        upload_btn.isEnabled = false
    }

    private fun showIdle() {
        progressBar.visibility = View.INVISIBLE
        selectFile_btn.isEnabled = true
        upload_btn.isEnabled = true
    }
}
