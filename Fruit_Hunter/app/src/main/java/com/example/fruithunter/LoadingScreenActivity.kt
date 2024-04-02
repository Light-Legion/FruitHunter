package com.example.fruithunter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.content.Intent
import android.os.Handler
import android.view.View
import android.view.Window

class LoadingScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_loading_screen)
        val progressBar: ProgressBar = findViewById(R.id.progress)

        // Создаем объект Handler для работы с задержками
        val handler = Handler()

        // Запускаем задачу для обновления прогресса каждые 100 миллисекунд в течение 3 секунд
        handler.postDelayed(object : Runnable {
            private var progress = 0

            override fun run() {
                // Увеличиваем прогресс
                progress += 1
                progressBar.progress = progress

                if (progress < progressBar.max) {
                    // Повторяем задачу
                    handler.postDelayed(this, 50)
                } else {
                    // Прогресс завершен, переходим на следующий экран
                    navigateToNextScreen()
                }
            }
        }, 100)
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Закрываем текущую активность, чтобы пользователь не мог вернуться по кнопке "назад"
    }
}
