package com.computoenlanube.nube.models

data class AddResponse(
    val add: Boolean?,
    val name: String?,
    val error: Boolean,
    val status: String?
)