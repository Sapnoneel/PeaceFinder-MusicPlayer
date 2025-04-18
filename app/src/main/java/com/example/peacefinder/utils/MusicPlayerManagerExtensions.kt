package com.example.peacefinder.utils

// Extension function to get shuffle state
fun MusicPlayerManager.getShuffle(): Boolean {
    return this.isShuffleEnabled.value
}
