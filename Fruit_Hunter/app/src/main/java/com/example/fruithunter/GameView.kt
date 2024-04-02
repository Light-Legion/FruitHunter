package com.example.fruithunter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Shader
import android.media.MediaPlayer
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.lang.ref.WeakReference
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    var onFruitSliced: (Int) -> Unit = {}
    var onFruitMissed: () -> Unit = {}
    private var timeSinceLastSpawn = 60
    private var maxCirclesOnScreen = 3
    private val gameLoopHandler = GameLoopHandler(this)
    private val paint = Paint()
    private val circles = mutableListOf<Circle>()
    private var swipeStart = PointF()
    private var swipeEnd = PointF()
    private val fragments = mutableListOf<Fragment>()
    private var gameStarted = false
    init {
        //gameLoopHandler.sendEmptyMessage(0) // Start the game loop
    }
    private val mediaPlayer: MediaPlayer = MediaPlayer.create(context, R.raw.slice_sound_trim).apply {
        setOnPreparedListener {
            seekTo(500) // Start at the 1 second mark
        }
    }
    companion object {
        private class GameLoopHandler(view: GameView) : Handler() {
            private val weakView: WeakReference<GameView> = WeakReference(view)
            override fun handleMessage(msg: Message) {
                val view = weakView.get()
                view?.updateGameState()
                view?.invalidate() // Redraw the view
                sendEmptyMessageDelayed(0, 1000 / 60) // 60 FPS
            }
        }
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!gameStarted) {
            gameLoopHandler.sendEmptyMessage(0) // Start the game loop
            gameStarted = true
        }
    }
    private var isGamePaused = false

    fun pauseGame() {
        isGamePaused = true
    }

    fun resumeGame() {
        isGamePaused = false
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isGamePaused) {
            // Draw circles
            paint.color = Color.GREEN
            circles.forEach { circle ->
                canvas.drawCircle(circle.x.toFloat(), circle.y, circle.radius, paint)
            }
            // Draw fragments
            fragments.forEach { fragment ->
                paint.color = fragment.color
                canvas.drawCircle(fragment.x, fragment.y, fragment.radius, paint)
            }
            // Draw swipe lines
            if (swipePoints.size > 1) {
                paint.strokeWidth = 10f
                for (i in 0 until swipePoints.size - 1) {
                    val startPoint = swipePoints[i].first
                    val endPoint = swipePoints[i + 1].first
                    val startAlpha = swipePoints[i].second
                    val endAlpha = swipePoints[i + 1].second
                    // Apply a gradient to the paint for the swipe line
                    paint.shader = LinearGradient(
                        startPoint.x, startPoint.y, endPoint.x, endPoint.y,
                        Color.argb(startAlpha, 255, 0, 0),
                        Color.argb(endAlpha, 255, 255, 255),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint)
                }
                paint.shader = null // Reset the paint shader
            }
        }

        // Дополнительные действия при паузе (например, нарисовать сообщение о паузе)
        if (isGamePaused) {
            // Например, нарисовать текст "Пауза" по центру экрана
            val paint = Paint()
            paint.textSize = 50f
            paint.color = Color.RED
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Пауза", width / 2f, height / 2f, paint)
        }
    }
    private fun createFragments(circle: Circle, numberOfFragments: Int) {
        val angleStep = 2 * Math.PI / numberOfFragments
        for (i in 0 until numberOfFragments) {
            val angle = angleStep * i
            val speedX = (4 + Random.nextFloat() * 6) * cos(angle).toFloat()
            val speedY = (4 + Random.nextFloat() * 6) * sin(angle).toFloat()
            // Generate a random fire explosion-like color
            val red = Random.nextInt(200, 256)
            val green = Random.nextInt(100, 256)
            val blue = Random.nextInt(0, 100)
            val color = Color.rgb(red, green, blue)
            fragments.add(Fragment(circle.x, circle.y, circle.radius / 4, speedX, speedY, color))
        }
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                swipeStart.set(event.x, event.y)
                swipePoints.clear() // Clear the list of swipe points
                swipePoints.add(Pair(PointF(event.x, event.y), 255)) // Add the starting point with full alpha
            }
            MotionEvent.ACTION_MOVE -> {
                swipeEnd.set(event.x, event.y)
                checkForSlice()
                swipeStart.set(event.x, event.y) // Update the swipeStart to the current position
                swipePoints.add(Pair(PointF(event.x, event.y), 255)) // Add the current point with full alpha
            }
            MotionEvent.ACTION_UP -> {
                swipeEnd.set(event.x, event.y)
                checkForSlice()
                swipePoints.clear() // Clear the list of swipe points
            }
        }
        invalidate() // Trigger a redraw
        return true
    }
    private fun updateGameState() {
        if (!isGamePaused) {
            timeSinceLastSpawn++
            val missedCircles = mutableListOf<Circle>()
            val updatedCircles = mutableListOf<Circle>()
            circles.forEach { circle ->
                val updatedCircle = circle.updatePosition()
                if (circle.y - circle.radius > height) {
                    // Handle missed circle (remove life, end the game, etc.)
                    onFruitMissed()
                    maxCirclesOnScreen = min(
                        maxCirclesOnScreen + 1,
                        10
                    ) // Increase the maximum number of circles on screen, up to 10
                    missedCircles.add(circle)
                } else {
                    updatedCircles.add(updatedCircle)
                }
            }
            circles.clear()
            circles.addAll(updatedCircles)
            circles.removeAll(missedCircles)
            fragments.forEachIndexed { index, fragment ->
                fragments[index] = fragment.updatePosition()
            }
            // Remove fragments that are off-screen
            fragments.removeAll { fragment ->
                fragment.y + fragment.radius < 0 || fragment.y - fragment.radius > height || fragment.x + fragment.radius < 0 || fragment.x - fragment.radius > width
            }
            // Update the alpha values of the swipe points and remove the ones with zero alpha
            val updatedSwipePoints = mutableListOf<Pair<PointF, Int>>()
            swipePoints.forEach { (point, alpha) ->
                if (alpha > 0) {
                    updatedSwipePoints.add(Pair(point, alpha - 10))
                }
            }
            swipePoints.clear()
            swipePoints.addAll(updatedSwipePoints)
            // Spawn a new circle if there are less than maxCirclesOnScreen on the screen
            if (circles.size < maxCirclesOnScreen && timeSinceLastSpawn >= 60) {
                circles.add(
                    Circle(
                        Random.nextInt(width).toFloat(),
                        height.toFloat(),
                        40f + Random.nextFloat() * 60f, // Circle size
                        0f,
                        10f + Random.nextFloat() * 10f
                    )
                )
                timeSinceLastSpawn = 0 // Reset the timeSinceLastSpawn back to 0
            }
        }
    }
    private fun checkForSlice() {
        val swipeVector = PointF(swipeEnd.x - swipeStart.x, swipeEnd.y - swipeStart.y)
        val swipeLength = swipeVector.length()
        circles.removeAll { circle ->
            val circleToSwipeStart = PointF(circle.x - swipeStart.x, circle.y - swipeStart.y)
            val dotProduct = swipeVector.x * circleToSwipeStart.x + swipeVector.y * circleToSwipeStart.y
            val projectionFactor = dotProduct / (swipeLength * swipeLength)
            if (projectionFactor < 0 || projectionFactor > 1) {
                // Circle center is not in the range of the swipe line segment
                false
            } else {
                val projectionPoint = PointF(swipeStart.x + projectionFactor * swipeVector.x, swipeStart.y + projectionFactor * swipeVector.y)
                val distanceSquared = (circle.x - projectionPoint.x) * (circle.x - projectionPoint.x) + (circle.y - projectionPoint.y) * (circle.y - projectionPoint.y)
                val isSliced = distanceSquared < circle.radius * circle.radius
                if (isSliced) {
                    onFruitSliced(1) // Call the onFruitSliced with the number of points
                    createFragments(circle, 8) // Create 8 fragments for the sliced circle
                    mediaPlayer.seekTo(500) // Set the starting point to the 1-second mark
                    mediaPlayer.start() // Play the sound
                }
                isSliced
            }
        }
    }
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mediaPlayer.release()
    }
    private data class Fragment(val x: Float, val y: Float, val radius: Float, val speedX: Float, var speedY: Float, val color: Int) {
        private val gravity = 0.3f
        fun updatePosition(): Fragment {
            val newY = y + speedY
            val newSpeedY = speedY + gravity
            return Fragment(x + speedX, newY, radius, speedX, newSpeedY, color)
        }
    }
    private data class Circle(val x: Float, val y: Float, val radius: Float, val speed: Float, var upwardAcceleration: Float) {
        private val gravity = 0.2f
        fun updatePosition(): Circle {
            val newSpeed = speed + upwardAcceleration
            val newY = y - newSpeed
            upwardAcceleration -= gravity // Decrease the upward acceleration by the gravity value
            return Circle(x, newY, radius, speed, upwardAcceleration)
        }
    }
    private data class SwipeLine(val start: PointF, val end: PointF, var alpha: Int, var shortenFactor: Float)
    private val swipeLines = mutableListOf<SwipeLine>()
    private val swipePoints = mutableListOf<Pair<PointF, Int>>()
}