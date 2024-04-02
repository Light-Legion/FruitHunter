package com.example.fruithunter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.app.AlertDialog
import android.content.Intent


class GameScreenActivity : AppCompatActivity() {
    private lateinit var scoreTextView: TextView
    private lateinit var livesTextView: TextView
    private lateinit var gameView: GameView
    private var score = 0
    private var lives = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        setContentView(R.layout.activity_game_screen)

        scoreTextView = findViewById(R.id.scoreTextView)
        livesTextView = findViewById(R.id.livesTextView)
        gameView = findViewById(R.id.gameView)
        gameView.onFruitSliced = { points ->
            updateScore(points)
        }
        gameView.onFruitMissed = {
            decreaseLives()
        }
        updateScoreTextView()
        updateLivesTextView()
    }

    private fun updateScore(points: Int) {
        score += points
        updateScoreTextView()
    }
    private fun decreaseLives() {
        lives--
        updateLivesTextView()
        if (lives <= 0) {
            gameOver()
        }
    }
    private fun updateScoreTextView() {
        scoreTextView.text = "Score: $score"
    }
    private fun updateLivesTextView() {
        livesTextView.text = "Lives: $lives"
    }
    private fun gameOver() {
        showResultDialog(score)
    }

    private fun showResultDialog(score: Int) {
        // Приостановить текущую активность
        gameView.pauseGame()

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Результат")
        builder.setMessage("Вы заработали $score очков")

        // Кнопка "Вернуться домой"
        builder.setPositiveButton("Вернуться домой") { dialog, which ->
            // Ваш код для обработки нажатия на кнопку "Вернуться домой"
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Закрыть текущую активность
        }

        val dialog: AlertDialog = builder.create()

        // Запрет на закрытие диалога при нажатии вне области диалога
        dialog.setCanceledOnTouchOutside(false)

        // Показать диалог
        dialog.show()
    }

    fun onClickGoStop(view: View) {
        // Пауза игры
        gameView.pauseGame()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("ПАУЗА")
            .setMessage("Игра на паузе")
            .setPositiveButton("Продолжить игру") { dialog, _ ->
                // Продолжить игру
                dialog.dismiss()
                // Возобновление игры
                gameView.resumeGame()
            }
            .setCancelable(false) // Запретить закрытие диалога кнопкой "Назад"
            .show()
    }
}