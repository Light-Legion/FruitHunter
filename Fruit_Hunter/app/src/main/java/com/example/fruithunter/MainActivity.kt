package com.example.fruithunter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder

class MainActivity : AppCompatActivity() {
    lateinit var backgroundMusicService: BackgroundMusicService
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BackgroundMusicService.LocalBinder
            backgroundMusicService = binder.getService()
            isServiceBound = true

            // Начинаем воспроизведение музыки
            backgroundMusicService.startMusic()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // Запускаем службу
        val serviceIntent = Intent(this, BackgroundMusicService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun onClickGoPlay(view : View){
        val intent = Intent(this, GameScreenActivity::class.java)
        startActivity(intent)
    }

    fun onClickSettings(view : View){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Отключаем службу при уничтожении активити
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}