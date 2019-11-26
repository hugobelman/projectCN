package com.computoenlanube.nube.models

class UploadResponse(
    val fieldname: String,
    val originalname: String,
    val encoding: String,
    val mimetype: String,
    val destination: String,
    val filename: String,
    val path: String,
    val size: Long
)