package com.computoenlanube.nube.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.computoenlanube.nube.R
import com.computoenlanube.nube.adapters.FilesAdapter
import com.computoenlanube.nube.models.ApiClient
import com.computoenlanube.nube.models.MetadataFile
import kotlinx.android.synthetic.main.activity_files.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FilesActivity : AppCompatActivity() {

    private lateinit var files: List<MetadataFile>
    private lateinit var authCookie: String
    private var loading = true
        set(value) {
            if (value) showLoading()
            else showIdle()
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)

        authCookie = ApiClient.getAuthCookie(this)!!

        upload_fab.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivityForResult(intent, 1)
        }

        files_lv.setOnItemClickListener { adapterView, view, i, l ->
            downloadFile(files[i])
        }

        getFilesAndShowInListView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_files, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout_menuItem -> {
                ApiClient.cloudService.logout(authCookie)
                ApiClient.deleteAuthCookie(this)

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                this.finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == 1 && resultCode == Activity.RESULT_OK -> {
                getFilesAndShowInListView()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getFilesAndShowInListView() {
        ApiClient.cloudService.getAllFiles(authCookie).enqueue(object : Callback<List<MetadataFile>> {
            override fun onFailure(call: Call<List<MetadataFile>>, t: Throwable) {
                Toast.makeText(this@FilesActivity, t.message, Toast.LENGTH_SHORT).show()
                loading = false
            }

            override fun onResponse(call: Call<List<MetadataFile>>, response: Response<List<MetadataFile>>) {
                files = response.body()!!

                loading = false
                this@FilesActivity.files_lv.adapter = FilesAdapter(this@FilesActivity, files)
            }

        })
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun showIdle() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun downloadFile(metadataFile: MetadataFile) {
        val uri = Uri.parse("${ApiClient.BASE_URL}/download/${metadataFile.titulo}")

        val request = DownloadManager.Request(uri)

        request.apply {
            addRequestHeader("Cookie", ApiClient.getAuthCookie(this@FilesActivity))
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            setTitle(metadataFile.nom_or)
            setDescription("Descargando ${metadataFile.nom_or}...")
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, metadataFile.nom_or)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
