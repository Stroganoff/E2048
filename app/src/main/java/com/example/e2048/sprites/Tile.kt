package com.example.e2048.sprites

import android.graphics.Canvas
import com.example.e2048.TileManager
import kotlin.math.pow
import kotlin.random.Random

class Tile(
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val standardSize: Int,
    val matrixX: Int,
    val matrixY: Int,
    private val callback: TileManager
) : Sprite {

    private var level = 1

    private var currentX: Int
    private var currentY: Int
    private var destinationX: Int
    private var destinationY: Int
    private var isMoving = false
    private val speed = 200
    private var levelUpInProgress = false
    private val chance = Random.Default.nextInt(100)

    init {
        currentX = xPositionInScreen(matrixY)
        currentY = yPositionInScreen(matrixX)
        destinationX = currentX
        destinationY = currentY
        decideInitialLevel()
    }

    fun setLevel(level: Int) {
        this.level = level
    }

    private fun decideInitialLevel() {
        if (chance > 90) {
            level = 2
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(
            callback.getBitmap(level),
            currentX.toFloat(),
            currentY.toFloat(),
            null
        )

        if (isMoving && currentX == destinationX && currentY == destinationY) {
            isMoving = false
            if (levelUpInProgress) {
                level++
                levelUpInProgress = false

                val newPoints = 2f.pow(level).toInt()
                callback.updateScore(newPoints)

                if (level == 11) {
                    callback.reached2048()
                }
            }
            callback.finishedMoving(this)
        }
    }

    fun move(matrixX: Int, matrixY: Int) {
        isMoving = true
        destinationX = xPositionInScreen(matrixY)
        destinationY = yPositionInScreen(matrixX)
    }

    fun getLevel(): Int {
        return level
    }

    fun levelUp(): Tile {
        levelUpInProgress = true
        return this
    }

    fun isLevelingUp() = levelUpInProgress

    override fun update() {
        currentX = updatePosition(currentX, destinationX)
        currentY = updatePosition(currentY, destinationY)
    }

    private fun updatePosition(current: Int, destination: Int) =
        if (current < destination) {
            if (current + speed > destination) {
                destination
            } else {
                current + speed
            }
        } else if (current > destination) {
            if (current - speed < destination) {
                destination
            } else {
                current - speed
            }
        } else destination

    private fun xPositionInScreen(matrixY: Int) = (screenWidth / 2) + ((matrixY - 2) * standardSize)
    private fun yPositionInScreen(matrixX: Int) = (screenHeight / 2) + ((matrixX - 2) * standardSize)
}