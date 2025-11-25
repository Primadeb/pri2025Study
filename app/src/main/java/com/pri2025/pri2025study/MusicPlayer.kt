package com.pri2025.pri2025study

import android.content.Context
import android.media.MediaPlayer

object MusicPlayer {

    private var mediaPlayer: MediaPlayer? = null

    // Play or resume music
    fun start(context: Context) {
        val mp = mediaPlayer
        if (mp == null) {
            // create new player with your file in res/raw/study_music.mp3
            mediaPlayer = MediaPlayer.create(
                context.applicationContext,
                R.raw.study_music   // make sure the file is named study_music.mp3
            ).apply {
                isLooping = true
                start()
            }
        } else {
            if (!mp.isPlaying) {
                mp.start()
            }
        }
    }

    // Pause music (can resume later with start)
    fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    // Completely stop & release (e.g. in onDestroy)
    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}