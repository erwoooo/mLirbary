package com.lucky.library.rv.view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.lucky.library.R


import com.lucky.library.rv.data.EventPosition
import com.lucky.library.rv.data.TimeEvent
import com.lucky.library.rv.data.TimeUit
import com.lucky.mod.play.rv.inter.TimeRecycleInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @Author cw
 * @Used
 * @Date 2025/2/19 11:57
 */
class TimeRecycleLayout(context: Context): RelativeLayout(context) {

    private val TAG = "TimeRecycleLayout"
    private var tvTopTime: TextView
    private var tvBtmTime: TextView
    private var timeView: TimeView
    private var ivFile: ImageView
    private var rlFile: RelativeLayout
    private var tvTime: TextView
    private var tvTitle: TextView
    private var tvFileTitle: TextView
    private var mLayoutType: Int = LAYOUT_CONTENT
    private var mLayoutHeight: Int = -1
    //当前item的开始时间
    private var mCurrentScaleStartTime : Int = -1
    //一个item的时间长度是多少
    private var mLongTimeDuration: Int = TimeUit.OneHour.timeDuration
    private var mTimeRecycleInterface: TimeRecycleInterface? = null
    private val mSimpleDateFormatInstance : SimpleDateFormat by lazy {
        SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    }

    private val mTimeEventList: ArrayList<TimeEvent> by lazy {
        arrayListOf()
    }
    private val mEventPositionList: ArrayList<EventPosition> by lazy {
        arrayListOf()
    }
    companion object{
        val LAYOUT_START: Int = 1
        val LAYOUT_CONTENT: Int = 2
        val LAYOUT_END: Int = 3
    }
    fun setTimeRecycleInterface(timeRecycleInterface: TimeRecycleInterface){
        mTimeRecycleInterface = timeRecycleInterface
    }
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.play_item_time_layout,this,false)
        tvTopTime = view.findViewById(R.id.leftTvTextTime)
        tvBtmTime = view.findViewById(R.id.rightTvTextTime)
        timeView = view.findViewById(R.id.tv_time)
        ivFile = view.findViewById(R.id.iv_file)
        rlFile = view.findViewById(R.id.rl_file)
        tvTime = view.findViewById(R.id.tv_long)
        tvTitle = view.findViewById(R.id.tv_title)
        tvFileTitle = view.findViewById(R.id.tv_title_file)
        addView(view)

        ivFile.setOnClickListener {
            mTimeRecycleInterface?.let {
                if (mEventPositionList.size > 1){
                    it.onTimeList(mTimeEventList)
                }else{
                    it.onTimePlay(mEventPositionList[0].realStartTime)
                }
            }
        }


    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        setEventToView()
    }

    /**
     * 设置数据源
     *
     * @param list
     */
    fun setEvent(list: List<TimeEvent>){
        mTimeEventList.clear()
        mTimeEventList.addAll(list)
        setEventToView(true)
    }

    private fun setEventToView(updateUi: Boolean = false) {
        mEventPositionList.clear()
        for (event in mTimeEventList){
            var startTime = event.startTime
            var endTime = event.endTime
            val eventPosition = EventPosition().apply {
                realStartTime = startTime
                realEndTime = endTime
                showPick = endTime <= getCurrentScaleEndTime()
            }

            if (startTime <= mCurrentScaleStartTime){
                startTime = mCurrentScaleStartTime
            }
            if (endTime >= getCurrentScaleEndTime()){
                endTime = getCurrentScaleEndTime()
            }
            eventPosition.apply {
                startX = findPositionByTime(startTime)
                startY = findPositionByTime(endTime)
            }
            mEventPositionList.add(eventPosition)
        }
        timeView.setEventList(mEventPositionList)

        if (!updateUi)
            return
        if (mEventPositionList.isEmpty()){
            ivFile.visibility = GONE
            rlFile.visibility = GONE
        }else{
            ivFile.visibility = VISIBLE
            rlFile.visibility = VISIBLE
            if (mEventPositionList.size > 1){
                ivFile.setImageResource(R.drawable.play_icon_file)
                tvFileTitle.text = "${mTimeEventList.size}"
                tvTime.text =""
                tvTitle.text =""
            }else{
                if (!mEventPositionList[0].showPick){
                    ivFile.visibility = GONE
                    rlFile.visibility = GONE
                }else{
                    ivFile.setImageResource(R.drawable.play_icon_file_s)
                    val txt =  mSimpleDateFormatInstance.format(Date(mEventPositionList[0].realStartTime * 1000L))
                    Log.e(
                        TAG,
                        "setEventToView: 在单个的模式下 txt = $txt  startTime = ${mEventPositionList[0].realStartTime}",

                        )
                    tvTitle.text = txt
                    tvTime.text = "${mEventPositionList[0].realEndTime - mEventPositionList[0].realStartTime}"
                    tvFileTitle.text = ""
                }
            }
        }

    }

    private fun findPositionByTime(time: Int): Int{
        var currentScaleStartTime = mCurrentScaleStartTime
        if (time < currentScaleStartTime || time > currentScaleStartTime + mLongTimeDuration){
            return  -1
        }
        return (getTimeViewHeight() - ((time - currentScaleStartTime) * 1f / mLongTimeDuration) * getTimeViewHeight()).toInt()
    }

    fun computePositionTime(y: Int): Int {
        val relativeY = y - (top + paddingTop)
        return (getCurrentScaleEndTime() - ((mLongTimeDuration * relativeY * 1f) / measuredHeight).toInt())
    }

    fun getTimeViewHeight(): Int = timeView.measuredHeight

    fun setLayoutHeight(height: Int){
        Log.e(TAG, "setLayoutHeight: mLayoutHeight $height", )
        if (height != measuredHeight){
            mLayoutHeight = height
        }
    }

    fun setLayoutType(layoutType: Int, padding: Int){
        when(layoutType){
            LAYOUT_START ->setPadding(0,padding,0,0)
            LAYOUT_END ->setPadding(0,0,0, (padding * 1.8).toInt())
        }
        mLayoutType = layoutType
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val layoutParams = getChildAt(0).layoutParams
        mLayoutHeight = if (mLayoutHeight < 0) 240 else mLayoutHeight

        if (layoutParams.height != mLayoutHeight){
            layoutParams.height = mLayoutHeight
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    /**
     * 设置当前view的开始时间
     *
     * @param curTime
     */
    fun setTvStartTime(curTime: Int,position: Boolean){
        Log.e(TAG, "setTvStartTime: curTime $curTime")
        mCurrentScaleStartTime = curTime
        tvTopTime.text = mSimpleDateFormatInstance.format(Date(getCurrentScaleEndTime() * 1000L))
        if (mLayoutType == LAYOUT_END){
            tvBtmTime.text = mSimpleDateFormatInstance.format(Date(curTime * 1000L))
        }
    }

    fun getCurrentScaleStartTime() = mCurrentScaleStartTime
    fun getCurrentScaleEndTime() = mCurrentScaleStartTime + mLongTimeDuration

    /**
     * 设置一个item的总时长
     *
     * @param duration
     */
    fun setLongTimeDuration(duration: Int){
        mLongTimeDuration = duration
    }

}