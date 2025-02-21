package com.lucky.library.rv.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.lucky.library.rv.data.EventPosition

/**
 * @Author cw
 * @Used
 * @Date 2025/2/19 13:43
 */
class TimeView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val TAG = "TimeListView"
    val paint = Paint().apply {
        color = Color.parseColor("#E5E5E5")
    }

    val paintRect = Paint().apply {
        color = Color.parseColor("#3155F5")
    }

    private val mEventPositionList: ArrayList<EventPosition> by lazy {
        arrayListOf()
    }

    private var mTop: Int = 0
    private var mLeft: Int = 0
    private var mRight: Int = 0
    private var mBottom: Int = 0


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mTop = paddingTop
        mLeft = paddingLeft
        mBottom = measuredHeight - paddingBottom
        mRight = measuredWidth - paddingRight
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas?.drawRect(mLeft.toFloat(), mTop.toFloat(), mRight.toFloat(), mBottom.toFloat(),paint)
        for (eventPosition in mEventPositionList){
            canvas?.drawRect(mLeft.toFloat(),
                eventPosition.startX.toFloat(), mRight.toFloat(),
                eventPosition.startY.toFloat(),paintRect)
        }
    }


    fun setEventList(list: List<EventPosition>) {
        mEventPositionList.clear()
        mEventPositionList.addAll(list)
        Log.e(TAG, "setEventList: $mEventPositionList", )
    }


}