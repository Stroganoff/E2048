package com.example.e2048

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import com.example.e2048.SwipeCallback.Direction
import kotlin.math.abs

class SwipeListener(
    context: Context,
    private val callback: SwipeCallback
) : OnGestureListener {

    private val gestureDetector = GestureDetector(context, this)

    override fun onFling(p0: MotionEvent?, p1: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (abs(velocityX) > abs(velocityY)) {
            if (velocityX > 0) {
                callback.onSwipe(Direction.RIGHT)
            } else {
                callback.onSwipe(Direction.LEFT)
            }
        } else {
            if (velocityY > 0) {
                callback.onSwipe(Direction.DOWN)
            } else {
                callback.onSwipe(Direction.UP)
            }
        }
        return false
    }

    fun onTouchEvent(event: MotionEvent) = gestureDetector.onTouchEvent(event)

    override fun onDown(p0: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(p0: MotionEvent) {

    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return false
    }

    override fun onLongPress(p0: MotionEvent) {

    }
}