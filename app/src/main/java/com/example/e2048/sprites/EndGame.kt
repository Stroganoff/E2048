package com.example.e2048.sprites

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import com.example.e2048.R

class EndGame(
    resources: Resources,
    private val screenWidth: Int,
    private val screenHeight: Int
) : Sprite {

    private val endgame = BitmapFactory.decodeResource(
        resources,
        R.drawable.gameover
    ).let {
        Bitmap.createScaledBitmap(
            it,
            resources.getDimension(R.dimen.endgame_width).toInt(),
            resources.getDimension(R.dimen.endgame_height).toInt(),
            false
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(
            endgame,
            (screenWidth - endgame.width) / 2f,
            (screenHeight - endgame.height) / 2f,
            null
        )
    }

    override fun update() {}
}