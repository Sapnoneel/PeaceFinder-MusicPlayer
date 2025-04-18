package com.example.peacefinder.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.peacefinder.model.Song

object SongLoader {

    fun loadAllSongs(context: Context): List<Song> {
        val songs = mutableListOf<Song>()

        val contentResolver = context.contentResolver
        val songUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA, // Path of song
            MediaStore.Audio.Media.DURATION
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val cursor = contentResolver.query(
            songUri,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val song = Song(
                    id = it.getLong(idColumn),
                    title = it.getString(titleColumn),
                    artist = it.getString(artistColumn),
                    path = it.getString(pathColumn),
                    duration = it.getLong(durationColumn)
                )
                songs.add(song)
            }
        }

        return songs
    }
}
