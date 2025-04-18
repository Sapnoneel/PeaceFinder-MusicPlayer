package com.example.peacefinder.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val path: String,
    val duration: Long,
    val uri: Uri
)
