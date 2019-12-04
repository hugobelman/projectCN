package com.computoenlanube.nube.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.computoenlanube.nube.R
import com.computoenlanube.nube.adapters.FilesAdapter
import com.computoenlanube.nube.models.ApiClient
import com.computoenlanube.nube.models.DeleteResponse
import com.computoenlanube.nube.models.MetadataFile
import com.google.android.material.snackbar.Snackbar
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
            val file = files[i]

            val dialog = AlertDialog.Builder(this).apply {
                setTitle("Archivo con extensión desconocida")
                setMessage("¿Desea descargar el archivo ${file.nom_or}")
                setPositiveButton("Si") { _, _ ->
                    downloadFile(file)
                }
                setNegativeButton("No", null)
            }

            dialog.show()
        }

        files_lv.setOnItemLongClickListener { adapterView, view, i, l ->
            val file = files[i]

            val dialog = AlertDialog.Builder(this).apply {
                setTitle("¿Desea eliminar este archivo?")
                setMessage("Se eliminará ${file.nom_or} y no se podrá recuperar")
                setPositiveButton("Si") { _: DialogInterface, _: Int ->
                    loading = true

                    ApiClient.cloudService.deleteFile(authCookie, file.id_archivos).enqueue(object : Callback<DeleteResponse> {
                        override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                            Toast.makeText(this@FilesActivity, t.message, Toast.LENGTH_SHORT).show()
                            loading = false
                        }

                        override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                            loading = false
                            val deleteResponse = response.body()

                            when {
                                deleteResponse?.error == null -> ApiClient.showError(this@FilesActivity, response.code())

                                !deleteResponse.error -> {
                                    Snackbar.make(view, "El archivo ha sido borrado correctamente", Snackbar.LENGTH_SHORT).show()
                                    getFilesAndShowInListView()
                                }

                                else -> Snackbar.make(view, deleteResponse.status ?: "El archivo no fue borrado: error en el servidor", Snackbar.LENGTH_SHORT).show()
                            }
                        }

                    })
                }
                setNegativeButton("No",null)
            }

            dialog.show()

            return@setOnItemLongClickListener true
        }


        changePass_btn.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivityForResult(intent, 2)
        }

        logout_btn.setOnClickListener {
            logout()
        }

        getFilesAndShowInListView()
    }

    private fun logout() {
        ApiClient.cloudService.logout(authCookie)
        ApiClient.deleteAuthCookie(this)

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            requestCode == 1 && resultCode == Activity.RESULT_OK -> {
                getFilesAndShowInListView()
            }

            requestCode == 2 && resultCode == Activity.RESULT_OK -> {
                logout()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getFilesAndShowInListView() {
        ApiClient.cloudService.getAllFiles(authCookie).enqueue(object : Callback<List<MetadataFile>> {
            override fun onFailure(call: Call<List<MetadataFile>>, t: Throwable) {
                 AlertDialog.Builder(this@FilesActivity).apply {
                     setTitle("Error al obtener los archivos")
                     setMessage("Ocurrio un error al obtener los archivos: ${t.message}")
                     setPositiveButton("Cerrar sesión") { _,_ ->
                         logout()
                     }
                     setCancelable(false)
                 }.show()

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1)
        } else {
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
}
