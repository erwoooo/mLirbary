package com.lucky.library.rv

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_POINTER_2_DOWN
import android.view.MotionEvent.ACTION_POINTER_3_DOWN
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lucky.library.rv.data.EventTimeComputeTool


import com.lucky.library.rv.data.TimeEvent
import com.lucky.library.rv.data.TimeUit
import com.lucky.library.rv.inter.TimeRecycleInterface
import com.lucky.library.rv.view.TimeRecycleLayout

/**
 * @Author cw
 * @Used
 * @Date 2025/2/19 11:34
 */
class TimeRecycleView(context: Context,attributeSet: AttributeSet): RecyclerView(context,attributeSet),
    ScaleGestureDetector.OnScaleGestureListener {
    private val TAG = "TimeRecycleView"
    private var mStartTime: Int = 0
    private var mEndTime: Int = 0
    private val mTimeEventList: ArrayList<TimeEvent> by lazy {
        arrayListOf()
    }

    private var mItemHeight: Int = 0
    private var mTempItemHeight: Int = 0
    private val mDefaultItemHeight: Int by lazy {
        240
    }

    private val mMinHeight: Float = mDefaultItemHeight * 0.8f
    private val mMaxHeight: Float = mDefaultItemHeight * 1.5f


    private var mTimeRecycleInterface : TimeRecycleInterface? = null
    private val mLinearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(context)
    }

    private var mItemTotalNumber: Int = 0
    private var mCurTimeUit: TimeUit = TimeUit.OneHour
    private val mTimeRecycleAdapter: TimeRecycleAdapter by lazy {
        TimeRecycleAdapter()
    }

    private val mOnScrollListener: OnScrollListener by lazy {
        object : OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when(newState){
                    SCROLL_STATE_IDLE->{
                        val view = findChildViewUnder(0f,mScrollYCenter.toFloat()) ?: return
                        val holder: TimeRecycleHolder = getChildViewHolder(view) as? TimeRecycleHolder
                            ?:return
                        val timeRecycleLayout: TimeRecycleLayout = holder.getView()
                        val firstPosition = holder.adapterPosition

                        mCenterLineIndicateTime =
                            timeRecycleLayout.computePositionTime(mScrollYCenter).toInt()
                    }
                }

            }
        }
    }
    private var mScaleGestureDetector: ScaleGestureDetector ?= null

    private var mLeft: Int = 0
    private var mTop: Int = 0
    private var mRight: Int = 0
    private var mBottom: Int = 0
    init {
        mItemHeight = mDefaultItemHeight
        mTempItemHeight = mDefaultItemHeight
        layoutManager = mLinearLayoutManager
        adapter = mTimeRecycleAdapter
        mScaleGestureDetector = ScaleGestureDetector(context,this)
        addOnScrollListener(mOnScrollListener)
    }


    private var mScrollYCenter: Int = 0

    private var mCenterLineIndicateTime: Int = 0

    fun setDayTime(startTime: Int,endTime: Int){
        mStartTime = startTime
        mEndTime = endTime
        calcItemSum()
    }

    fun setTimeRecycleInterface(timeRecycleInterface: TimeRecycleInterface){
        mTimeRecycleInterface = timeRecycleInterface
    }

    fun setDataSource(list: List<TimeEvent>){
        mTimeEventList.clear()
        mTimeEventList.addAll(list)
    }


    private fun calcItemSum(){
        mItemTotalNumber = (mEndTime - mStartTime) / mCurTimeUit.timeDuration
    }




    inner class TimeRecycleHolder(val view: View): ViewHolder(view){
        fun getView(): TimeRecycleLayout {
            return view as TimeRecycleLayout
        }
    }

    inner class TimeRecycleAdapter : Adapter<TimeRecycleHolder>(){
        private var mViewGroup: ViewGroup? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeRecycleHolder {
            if (mViewGroup == null){
                mViewGroup = parent
            }
            val timeRecycleLayout = TimeRecycleLayout(parent.context)
            mTimeRecycleInterface?.let { timeRecycleLayout.setTimeRecycleInterface(it) }
            return TimeRecycleHolder(timeRecycleLayout)
        }

        override fun getItemViewType(position: Int): Int {
            return when(position){
                0 -> TimeRecycleLayout.LAYOUT_START
                (itemCount - 1)-> TimeRecycleLayout.LAYOUT_END
                else-> TimeRecycleLayout.LAYOUT_CONTENT
            }

        }

        fun getItemPositionByTime(time: Int): Int{
            val position  = (mEndTime - time) / mCurTimeUit.timeDuration
            return position.takeIf { position < mItemTotalNumber } ?: (mItemTotalNumber - 1)
        }

        override fun getItemCount(): Int {
            return mItemTotalNumber
        }

         fun getItem(position: Int): Int{
           return if (position < mItemTotalNumber){
                (mEndTime - (position + 1) * mCurTimeUit.timeDuration)
            }else{
                0
            }
        }



        override fun onBindViewHolder(holder: TimeRecycleHolder, position: Int) {
            with(holder.getView()){
                setLayoutHeight(mItemHeight)
                setLayoutType(getItemViewType(position),(mViewGroup?.height?: mItemHeight) / 2)
                setTvStartTime(getItem(position),position == (mItemTotalNumber -1))
                setLongTimeDuration(mCurTimeUit.timeDuration)
                setEvent(EventTimeComputeTool.findEventTimeList(getCurrentScaleStartTime(),getCurrentScaleEndTime(),mTimeEventList))
            }
        }

    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleFactor = detector.scaleFactor

        if (scaleFactor == 1f){
            setScaleFactor(scaleFactor)
        }else{
            val height = setScaleFactor(scaleFactor)
            if(height > 0){
                val newPosition = mTimeRecycleAdapter.getItemPositionByTime(mCenterLineIndicateTime)
                val duration = mCurTimeUit.timeDuration
                var offsetY = (mScrollYCenter - (mTimeRecycleAdapter.getItem(newPosition) + duration - mCenterLineIndicateTime) * 1f * height / duration).toInt()
                if (newPosition == 0){
                    offsetY = -mScrollYCenter
                }
                Log.e(TAG, "onScale: newPosition = $newPosition offsetY= $offsetY  mScrollYCenter= $mScrollYCenter")
                mLinearLayoutManager.scrollToPositionWithOffset(newPosition,offsetY)
            }
        }

        return false
    }

    private var mScaleFactor: Float = 1f
    private fun setScaleFactor(scaleFactor: Float) : Int {
        val curTimeUit = mCurTimeUit
        if (scaleFactor == 1f){
            mScaleFactor = scaleFactor
            mTempItemHeight = mItemHeight.takeIf { it != -1 }?: mDefaultItemHeight
            return  -1
        }

        val order = TimeUit.orderByDes.takeIf { scaleFactor > 1f }?: TimeUit.orderByAsc

        val nextTimeUit = TimeUit.getNextTimeUit(curTimeUit,order)

        if (curTimeUit == nextTimeUit && mItemHeight == mDefaultItemHeight){
            return  -1
        }

        if (order == TimeUit.orderByDes){
            mScaleFactor += 0.03f
        }else{
            mScaleFactor -= 0.03f
        }

        mItemHeight = (mTempItemHeight * (mScaleFactor)).toInt()

        Log.e(TAG, "setScaleFactor: mItemHeight= $mItemHeight  scaleFactor= $scaleFactor")
        if ((scaleFactor > 1 && mItemHeight >= mMaxHeight) || (scaleFactor < 1 && mItemHeight <= mMinHeight)){
            mScaleFactor = 1f
            mCurTimeUit = nextTimeUit
            calcItemSum()
            mItemHeight = mDefaultItemHeight
            mTempItemHeight = mItemHeight
        }
        mTimeRecycleAdapter.notifyDataSetChanged()
        return mItemHeight
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        if (e?.pointerCount == 2 && e.action != MotionEvent.ACTION_CANCEL){
            when(e.action){
                ACTION_POINTER_DOWN,ACTION_POINTER_2_DOWN,ACTION_POINTER_3_DOWN->setScaleFactor(1f)
            }

            return mScaleGestureDetector?.onTouchEvent(e)?:false
        }
        return super.onTouchEvent(e)
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        mLeft = paddingLeft
        mTop = paddingTop
        mRight = measuredWidth - paddingRight
        mBottom = measuredHeight - paddingBottom
        mScrollYCenter = mTop + (mBottom - mTop) / 2
        Log.e(TAG, "onLayout: recycleview mScrollYCenter= $mScrollYCenter")
    }


    /**
     * 滑动到指定时间轴
     *
     * @param time
     */
    fun scrollToTimePosition(time: Int){
        val timeRecycleLayout: TimeRecycleLayout = findChildViewUnder(0f, mScrollYCenter.toFloat()) as? TimeRecycleLayout
            ?:return
        val viewHeight = timeRecycleLayout.getTimeViewHeight()
        scrollToTimePosition(time,viewHeight)
    }

    /**
     * 设置滑动后的中心位置
     *
     * @param time
     * @param viewHeight
     */
    private fun scrollToTimePosition(time: Int,viewHeight: Int){
        val scrollTime = time.takeIf { it <= 0 }?:mStartTime
        val newPosition = mTimeRecycleAdapter.getItemPositionByTime(scrollTime)
        var offsetY = (mScrollYCenter - (mTimeRecycleAdapter.getItem(newPosition) + mCurTimeUit.timeDuration - scrollTime) * 1f * viewHeight / mCurTimeUit.timeDuration).toInt()
        if (newPosition == 0){
            offsetY = -mScrollYCenter
        }
        Log.e(TAG, "scrollToTimePosition: newPosition = $newPosition offsetY= $offsetY")
        mLinearLayoutManager.scrollToPositionWithOffset(newPosition,offsetY)
        mCenterLineIndicateTime = scrollTime
    }
}