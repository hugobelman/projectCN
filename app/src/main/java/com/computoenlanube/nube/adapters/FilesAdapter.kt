package com.computoenlanube.nube.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.computoenlanube.nube.R
import com.computoenlanube.nube.models.MetadataFile
import kotlinx.android.synthetic.main.item_file.view.*



class FilesAdapter(private val mContext: Context, private val files: List<MetadataFile>) : ArrayAdapter<MetadataFile>(mContext, 0, files) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout =  LayoutInflater.from(mContext).inflate(R.layout.item_file, parent, false)

        val file = files[position]

        layout.fileName_tv.text = file.nom_or
        layout.size_tv.text = humanReadableByteCount(file.size)

        return layout
    }

    private fun humanReadableByteCount(bytes: Long, si: Boolean = true): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "KMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }
}