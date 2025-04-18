package com.example.peacefinder.utils

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.peacefinder.model.Song

class MusicPlayerManager(private val context: Context) {

    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private val songs = mutableListOf<Song>()
    private var currentSongIndex: Int = 0
    private var currentVolume: Float = 1f
    private var currentSong: Song? = null

    fun playSong(song: Song) {
        exoPlayer.setMediaItem(MediaItem.fromUri(song.path))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun play(song: Song) {
        currentSong = song
        exoPlayer.setMediaItem(MediaItem.fromUri(song.uri))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.seekTo(0) // Optional: Reset to the beginning of the song
    }

    fun release() {
        exoPlayer.release()
    }

    fun getCurrentPosition(): Long {
        return exoPlayer.currentPosition
    }

    fun getDuration(): Long {
        return exoPlayer.duration
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        exoPlayer.volume = currentVolume
    }


    fun getVolume(): Float {
        return exoPlayer.let {
            // Assuming L & R volumes are the same
            // Android doesn't give a direct getter, so store your volume state manually
            currentVolume
        }
    }

    enum class RepeatMode {
        NONE, ALL, ONE
    }

    internal var isShuffleEnabled = mutableStateOf(false)
    private var repeatMode = mutableStateOf(RepeatMode.NONE)

    fun toggleShuffle() {
        isShuffleEnabled.value = !isShuffleEnabled.value
    }

    fun getShuffleState(): Boolean {
        return isShuffleEnabled.value
    }

    fun cycleRepeatMode() {
        repeatMode.value = when (repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        repeatMode.value = mode
    }

    fun getRepeatMode(): RepeatMode {
        return repeatMode.value
    }

    fun playNextSong() {
        currentSongIndex = when {
            repeatMode.value == RepeatMode.ONE -> currentSongIndex // Replay same song
            isShuffleEnabled.value -> songs.indices.random()      // Shuffle mode
            else -> (currentSongIndex + 1) % songs.size           // Next song in queue
        }
        play(songs[currentSongIndex])
    }

    fun playPreviousSong() {
        currentSongIndex = when {
            repeatMode.value == RepeatMode.ONE -> currentSongIndex // Replay the same song
            isShuffleEnabled.value -> songs.indices.random()      // Shuffle mode
            else -> if (currentSongIndex == 0) songs.size - 1 else currentSongIndex - 1 // Go to previous song, or loop back to the last song
        }
        play(songs[currentSongIndex])
    }
}