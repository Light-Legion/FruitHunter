package com.example.fruithunter

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class BackgroundMusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BackgroundMusicService = this@BackgroundMusicService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        // Инициализируем MediaPlayer с ресурсом из папки raw
        mediaPlayer = MediaPlayer.create(this, R.raw.music1)
        mediaPlayer.isLooping = true
    }

    fun startMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun stopMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun setMusicSource(resourceId: Int) {
        // Остановить воспроизведение текущей музыки
        stopMusic()

        // Инициализировать MediaPlayer с новым ресурсом
        mediaPlayer = MediaPlayer.create(this, resourceId)
        mediaPlayer.isLooping = true

        // Начать воспроизведение новой музыки, если она включена
        if (isMusicEnabled) {
            startMusic()
        }
    }

    private val isMusicEnabled: Boolean
        get() {
            // Получить значение из SharedPreferences или другого источника
            // В данном контексте предполагается, что вы используете SharedPreferences
            val preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            return preferences.getBoolean("music_enabled", true)
        }

    override fun onDestroy() {
        super.onDestroy()

        // Освобождаем ресурсы MediaPlayer при уничтожении службы
        mediaPlayer.release()
    }
}