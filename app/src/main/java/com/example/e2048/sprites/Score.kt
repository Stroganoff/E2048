package com.example.e2048.sprites

import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import com.example.e2048.R

class Score(
    resources: Resources,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val standardSize: Int,
    private val sharedPreferences: SharedPreferences
) : Sprite {

    private var score: Int = 0
    private var topScore: Int = sharedPreferences.getInt(TOP_SCORE_PREFERENCES_KEY, 0)
    private var topScoreBonus = false
    private var reached2048Bonus = false

    private val scorePaint = Paint().apply {
        color = BLACK
        style = FILL
        textSize = resources.getDimension(R.dimen.score_text_size)
    }

    private val scoreBitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.score
    ).let {
        Bitmap.createScaledBitmap(
            it,
            resources.getDimension(R.dimen.score_label_width).toInt(),
            resources.getDimension(R.dimen.score_label_height).toInt(),
            false
        )
    }

    private val topScoreBitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.topscore
    ).let {
        Bitmap.createScaledBitmap(
            it,
            resources.getDimension(R.dimen.score_label_width).toInt(),
            resources.getDimension(R.dimen.score_label_height).toInt(),
            false
        )
    }

    private val topScoreBonusBitmap by lazy {
        BitmapFactory.decodeResource(
            resources,
            R.drawable.highscore
        ).let {
            Bitmap.createScaledBitmap(
                it,
                resources.getDimension(R.dimen.score_bonus_width).toInt(),
                resources.getDimension(R.dimen.score_bonus_height).toInt(),
                false
            )
        }
    }

    private val reached2048BonusBitmap by lazy {
        BitmapFactory.decodeResource(
            resources,
            R.drawable.a2048
        ).let {
            Bitmap.createScaledBitmap(
                it,
                resources.getDimension(R.dimen.score_bonus_width).toInt(),
                resources.getDimension(R.dimen.score_bonus_height).toInt(),
                false
            )
        }
    }

    override fun draw(canvas: Canvas) {
        drawScore(canvas)
        drawTopScore(canvas)

        if (topScoreBonus) {
            drawTopScoreBonus(canvas)
        }

        if (reached2048Bonus) {
            drawReached2048Bonus(canvas)
        }
    }

    private fun drawScore(canvas: Canvas) {
        val scoreTextSize = scorePaint.measureText(score.toString())

        canvas.drawBitmap(
            scoreBitmap,
            screenWidth / 4f - scoreBitmap.width / 2f,
            scoreBitmap.height.toFloat(),
            null
        )

        canvas.drawText(
            score.toString(),
            screenWidth / 4f - scoreTextSize / 2f,
            scoreBitmap.height * 4f,
            scorePaint
        )
    }

    private fun drawTopScore(canvas: Canvas) {
        val topScoreTextSize = scorePaint.measureText(topScore.toString())

        canvas.drawBitmap(
            topScoreBitmap,
            3f * screenWidth / 4f - topScoreBitmap.width / 2f,
            topScoreBitmap.height.toFloat(),
            null
        )

        canvas.drawText(
            topScore.toString(),
            3f * screenWidth / 4f - topScoreTextSize / 2f,
            scoreBitmap.height * 4f,
            scorePaint
        )
    }

    private fun drawTopScoreBonus(canvas: Canvas) {
        canvas.drawBitmap(
            topScoreBonusBitmap,
            screenWidth / 2f - 2f * standardSize,
            screenHeight / 2f - 2f * standardSize - 2f * topScoreBonusBitmap.height,
            null
        )
    }

    private fun drawReached2048Bonus(canvas: Canvas) {
        canvas.drawBitmap(
            reached2048BonusBitmap,
            screenWidth / 2f - 2f * standardSize,
            screenHeight / 2f - 2f * standardSize - 7f * reached2048BonusBitmap.height / 2f,
            null
        )
    }

    override fun update() {}

    fun updateScore(newPoints: Int) {
        score += newPoints
        checkTopScore()
    }

    fun reached2048() {
        reached2048Bonus = true
    }

    private fun checkTopScore() {
        topScore = sharedPreferences.getInt(TOP_SCORE_PREFERENCES_KEY, 0)
        if (score > topScore) {
            topScore = score
            sharedPreferences.edit().putInt(TOP_SCORE_PREFERENCES_KEY, score).apply()
            topScoreBonus = true
        }
    }

    companion object {
        private const val TOP_SCORE_PREFERENCES_KEY = "E2048.preferences.top_score"
    }
}