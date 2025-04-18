package com.example.peacefinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.peacefinder.model.Song
import com.example.peacefinder.ui.theme.PeaceFinderTheme
import com.example.peacefinder.utils.MusicPlayerManager
import com.example.peacefinder.utils.MusicPlayerManager.RepeatMode
import com.example.peacefinder.utils.SongLoader
import com.example.peacefinder.utils.getShuffle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay


@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PeaceFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MusicScreen() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val songs = remember { mutableStateOf(emptyList<Song>()) }
    val currentSongIndex = remember { mutableIntStateOf(-1) }
    val isPlaying = remember { mutableStateOf(false) }
    val playerManager = remember { MusicPlayerManager(context) }
    val currentPosition = remember { mutableLongStateOf(0L) }
    val songDuration = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            currentPosition.longValue = playerManager.getCurrentPosition()
            songDuration.longValue = playerManager.getDuration()
            delay(500L) // update every 0.5 seconds
        }
    }

    if (permissionState.status.isGranted) {
        songs.value = SongLoader.loadAllSongs(context)

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                itemsIndexed(songs.value) { index, song ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                currentSongIndex.intValue = index
                                playerManager.playSong(song)
                                isPlaying.value = true
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(text = song.title, style = MaterialTheme.typography.bodyLarge)
                        Text(text = song.artist, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 🎵 Bottom Music Control Bar
            var sliderPosition by remember { mutableFloatStateOf(0f) }
            var duration by remember { mutableLongStateOf(0L) }
            var volume by remember { mutableFloatStateOf(playerManager.getVolume()) }
            var repeatMode by remember { mutableStateOf(RepeatMode.NONE) } // ✅ use the enum
            val currentSong = songs.value[currentSongIndex.intValue]

            // Sync progress every second
            LaunchedEffect(currentSongIndex.intValue, isPlaying.value) {
                while (isPlaying.value) {
                    duration = playerManager.getDuration()
                    sliderPosition = playerManager.getCurrentPosition().toFloat()
                    delay(1000L)
                }
            }

            Column {
                // Top part of the screen (song name, artist, etc.)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Song info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = currentSong.title, style = MaterialTheme.typography.bodyLarge)
                        Text(text = currentSong.artist, style = MaterialTheme.typography.bodySmall)
                    }

                    // Playback controls (Play/Pause, Next, Previous)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(text = "⏮", modifier = Modifier.clickable {
                            if (currentSongIndex.intValue > 0) {
                                currentSongIndex.intValue--
                                val prev = songs.value[currentSongIndex.intValue]
                                playerManager.playSong(prev)
                                isPlaying.value = true
                            }
                        })

                        Text(
                            text = if (isPlaying.value) "⏸" else "▶️",
                            modifier = Modifier.clickable {
                                if (isPlaying.value) {
                                    playerManager.pause()
                                } else {
                                    playerManager.playSong(currentSong)
                                }
                                isPlaying.value = !isPlaying.value
                            }
                        )

                        Text(
                            text = "⏭",
                            modifier = Modifier.clickable {
                                playerManager.playNextSong() // Call playNextSong() to handle the next song
                                isPlaying.value = true // Set playing state
                            }
                        )

                        val isShuffle = remember { derivedStateOf { playerManager.getShuffleState() } }
                        val repeatMode = remember { derivedStateOf { playerManager.getRepeatMode() } }

                        IconButton(onClick = {
                            playerManager.toggleShuffle()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (isShuffle.value) Color.Green else Color.White
                            )
                        }

                        IconButton(onClick = {
                            playerManager.cycleRepeatMode()
                        }) {
                            val repeatIcon = when (repeatMode.value) {
                                RepeatMode.ALL -> Icons.Filled.Repeat
                                RepeatMode.ONE -> Icons.Filled.RepeatOne
                                RepeatMode.NONE -> Icons.Filled.Repeat
                            }

                            Icon(
                                imageVector = repeatIcon,
                                contentDescription = "Repeat",
                                tint = if (repeatMode.value != RepeatMode.NONE) Color.Green else Color.White
                            )
                        }
                    }
                }

                // Progress Bar
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    onValueChangeFinished = {
                        playerManager.seekTo(sliderPosition.toLong())
                    },
                    valueRange = 0f..(duration.takeIf { it > 0 } ?: 1).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(sliderPosition.toLong()))
                    Text(text = formatTime(duration))
                }

                // Volume control slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    val volumeIcon = when {
                        volume == 0f -> Icons.AutoMirrored.Filled.VolumeOff
                        volume < 0.5f -> Icons.AutoMirrored.Filled.VolumeDown
                        else -> Icons.AutoMirrored.Filled.VolumeUp
                    }

                    Icon(
                        imageVector = volumeIcon,
                        contentDescription = "Volume Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            playerManager.setVolume(it)
                        },
                        valueRange = 0f..1f,
                        steps = 10,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Shuffle and Repeat Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    // Shuffle Button
                    IconButton(onClick = {
                        playerManager.toggleShuffle()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (playerManager.getShuffle()) Color.Green else Color.Gray
                        )
                    }

                    // Repeat Button
                    IconButton(onClick = {
                        playerManager.setRepeatMode(
                            when (playerManager.getRepeatMode()) {
                                RepeatMode.NONE -> RepeatMode.ONE
                                RepeatMode.ONE -> RepeatMode.ALL
                                RepeatMode.ALL -> RepeatMode.NONE
                            }
                        )
                    }) {
                        val repeatIcon = when (repeatMode) {
                            RepeatMode.ALL -> Icons.Filled.Repeat
                            RepeatMode.ONE -> Icons.Filled.RepeatOne
                            else -> Icons.Filled.Repeat // for NONE or fallback
                        }

                        Icon(
                            imageVector = repeatIcon,
                            contentDescription = "Repeat",
                            tint = if (playerManager.getRepeatMode() != RepeatMode.NONE) Color.Green else Color.Gray
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Slider(
                    value = currentPosition.longValue.toFloat(),
                    onValueChange = { newValue ->
                        currentPosition.longValue = newValue.toLong()
                    },
                    onValueChangeFinished = {
                        playerManager.seekTo(currentPosition.longValue)
                    },
                    valueRange = 0f..(songDuration.longValue.coerceAtLeast(1)).toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional time display
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatTime(currentPosition.longValue), style = MaterialTheme.typography.bodySmall)
                    Text(formatTime(songDuration.longValue), style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Play Previous Button
                IconButton(onClick = { playerManager.playPreviousSong() }) {
                    Icon(imageVector = Icons.Filled.Replay, contentDescription = "Previous Song")
                }

                // Play/Pause Button
                IconButton(onClick = { playerManager.playNextSong() }) {
                    Icon(imageVector = Icons.Filled.Pause, contentDescription = "Play/Pause")
                }

                // Stop Button
                IconButton(onClick = { playerManager.stop() }) {
                    Icon(imageVector = Icons.Filled.Stop, contentDescription = "Stop")
                }

                // Release Button
                IconButton(onClick = { playerManager.release() }) {
                    Icon(imageVector = Icons.Filled.Stop, contentDescription = "Release")
                }
            }
        }
    } else {
        Text(text = "Permission Required to access Songs!")
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
