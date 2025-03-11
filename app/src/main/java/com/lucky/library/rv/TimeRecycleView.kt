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
import com.lucky.mod.play.rv.inter.TimeRecycleInterface
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
    private var mPlayAutoScroll = false
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

    /**
     * 滚动监听
     */
    private val mOnScrollListener: OnScrollListener by lazy {
        object : OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when(newState){
                    SCROLL_STATE_IDLE->{
                        mCenterLineIndicateTime =
                            getRelativePositionTime(mScrollYCenter)
                        if (mDragByUser){
                            mDragByUser = false
                            mTopLineIndicateTime = getRelativePositionTime(mScrollYTop)
                            Log.e(
                                TAG,
                                "onScrollStateChanged: mTopLineIndicateTime $mTopLineIndicateTime",

                                )
                            mTimeRecycleInterface?.onScrollTimePlay(mTopLineIndicateTime)
                        }
                    }

                    SCROLL_STATE_DRAGGING->{
                        mDragByUser = true
                        pauseAutoPlayScrolled()
                    }
                    SCROLL_STATE_SETTLING->{
                        mDragByUser = false
                        Log.e(TAG, "onScrollStateChanged: 我在自动滚动")
                    }
                }

            }
        }
    }
    private var mScaleGestureDetector: ScaleGestureDetector ?= null

    private var mDragByUser: Boolean = false
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


    /**
     * 中心位置在频幕上的位置
     */
    private var mScrollYCenter: Int = 0

    /**
     * 顶部位置在屏幕上的位置
     */
    private var mScrollYTop: Int = 0

    /**
     * 顶部位置的时间戳
     */
    private var mTopLineIndicateTime: Int = 0
    /**
     * 中心位置的时间戳
     */
    private var mCenterLineIndicateTime: Int = 0

    /**
     * 设置开始时间和结束时间
     *
     * @param startTime
     * @param endTime
     */
    fun setDayTime(startTime: Int,endTime: Int){
        mStartTime = startTime
        mEndTime = endTime
        calcItemSum()
    }

    /**
     * 回调接口
     *
     * @param timeRecycleInterface
     */
    fun setTimeRecycleInterface(timeRecycleInterface: TimeRecycleInterface){
        mTimeRecycleInterface = timeRecycleInterface
    }

    /**
     * 设置数据源
     *
     * @param list
     */
    fun setDataSource(list: List<TimeEvent>, startTime: Int, endTime: Int){
        Log.e(TAG, "setDataSource: 开始时间 $startTime  结束时间 $endTime")
        mTimeEventList.clear()
        mTimeEventList.addAll(list)
        mStartTime = startTime
        mEndTime = endTime
        calcItemSum()
        mTimeRecycleAdapter.notifyDataSetChanged()
    }

    fun getRelativePositionTime(relativeY: Int): Int{
        val view = findChildViewUnder(0f,relativeY.toFloat()) ?: return - 1
        val holder: TimeRecycleHolder = getChildViewHolder(view) as? TimeRecycleHolder
            ?:return -1
        val timeRecycleLayout: TimeRecycleLayout = holder.getView()
        return  timeRecycleLayout.computePositionTime(relativeY)
    }

    /**
     * 计算item个数
     *
     */
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
            Log.e(TAG, "getItemPositionByTime: position $position")
            return position.takeIf { position < mItemTotalNumber } ?: (mItemTotalNumber - 1)
        }

        override fun getItemCount(): Int {
            return mItemTotalNumber
        }

        fun getItem(position: Int): Int{
            return if (position < mItemTotalNumber){
                mEndTime - ((position + 1) * mCurTimeUit.timeDuration)
            }else{
                0
            }
        }



        override fun onBindViewHolder(holder: TimeRecycleHolder, position: Int) {
            with(holder.getView()){
                setLayoutHeight(mItemHeight)
                setLayoutType(getItemViewType(position),(mViewGroup?.height?: mItemHeight) / 2)
                setLongTimeDuration(mCurTimeUit.timeDuration)
                setTvStartTime(getItem(position),position == (mItemTotalNumber -1))
                setEvent(EventTimeComputeTool.findEventTimeList(getCurrentScaleStartTime(),getCurrentScaleEndTime(),mTimeEventList))
            }
        }

    }

    /**
     * 缩放
     *
     * @param detector
     * @return
     */
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

    /**
     * 缩放因子
     */
    private var mScaleFactor: Float = 1f

    /**
     * 设置缩放，并修改item高度
     *
     * @param scaleFactor
     * @return
     */
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

        mScrollYTop = mTop + 50
        Log.e(TAG, "onLayout: recycleview mScrollYCenter= $mScrollYCenter  mScrollYTop= $mScrollYTop")
    }


    /**
     * 滑动到指定时间轴
     *
     * @param time
     */
    fun scrollToTimePosition(time: Int){
        Log.e(TAG, "scrollToTimePosition: time $time")
        val timeRecycleLayout: TimeRecycleLayout = findChildViewUnder(0f, mScrollYCenter.toFloat()) as? TimeRecycleLayout
            ?:return
        val viewHeight = timeRecycleLayout.getTimeViewHeight()
        scrollToTimePosition(time,viewHeight)
    }

    /**
     * 滚动到顶部
     *
     * @param time
     */
    fun scrollToTimeTopPosition(time: Int){
        val timeRecycleLayout: TimeRecycleLayout = findChildViewUnder(0f, mScrollYTop.toFloat()) as? TimeRecycleLayout
            ?:return
        val viewHeight = timeRecycleLayout.getTimeViewHeight()
        scrollToTimeTopPosition(time,viewHeight)
    }
    private fun scrollToTimeTopPosition(time: Int,viewHeight: Int){
        val scrollTime = mStartTime.coerceAtLeast(time)
        Log.e(TAG, "scrollToTimePosition: scrollTime  $scrollTime  time $time")
        val newPosition = mTimeRecycleAdapter.getItemPositionByTime(scrollTime)
        var offsetY = (mScrollYTop - (mTimeRecycleAdapter.getItem(newPosition) + mCurTimeUit.timeDuration - scrollTime) * 1f * viewHeight / mCurTimeUit.timeDuration).toInt()
        if (newPosition == 0){
            offsetY = -mScrollYTop
        }
        Log.e(TAG, "scrollToTimePosition: newPosition = $newPosition offsetY= $offsetY")
        mLinearLayoutManager.scrollToPositionWithOffset(newPosition,offsetY)
    }


    /**
     * 设置滑动后的中心位置
     *
     * @param time
     * @param viewHeight
     */
    private fun scrollToTimePosition(time: Int,viewHeight: Int){
        val scrollTime = mStartTime.coerceAtLeast(time)
        Log.e(TAG, "scrollToTimePosition: scrollTime  $scrollTime  time $time")
        val newPosition = mTimeRecycleAdapter.getItemPositionByTime(scrollTime)
        var offsetY = (mScrollYCenter - (mTimeRecycleAdapter.getItem(newPosition) + mCurTimeUit.timeDuration - scrollTime) * 1f * viewHeight / mCurTimeUit.timeDuration).toInt()
        if (newPosition == 0){
            offsetY = -mScrollYCenter
        }
        Log.e(TAG, "scrollToTimePosition: newPosition = $newPosition offsetY= $offsetY")
        mLinearLayoutManager.scrollToPositionWithOffset(newPosition,offsetY)
        mCenterLineIndicateTime = scrollTime
    }


    /**
     * 控制当前缩放
     * 每次缩放之后，要拿到新的中心时间戳mCenterLineIndicateTime
     * 以mCenterLineIndicateTime为中心缩放
     * @param scale
     */
    fun setTimeUit(scale: Int) : TimeUit {
        Log.e(TAG, "setTimeUit: scale $scale")
        val nextTimeUit = TimeUit.getNextTimeUit(mCurTimeUit,scale)
        if (mCurTimeUit == nextTimeUit && mItemHeight == mDefaultItemHeight){
            return  mCurTimeUit
        }
        mScaleFactor = 1f
        mCurTimeUit = nextTimeUit
        calcItemSum()
        mItemHeight = mDefaultItemHeight
        mTempItemHeight = mItemHeight

        mTimeRecycleAdapter.notifyDataSetChanged()
        val newPosition = mTimeRecycleAdapter.getItemPositionByTime(mCenterLineIndicateTime)
        val duration = mCurTimeUit.timeDuration
        var offsetY = (mScrollYCenter - (mTimeRecycleAdapter.getItem(newPosition) + duration - mCenterLineIndicateTime) * 1f * height / duration).toInt()
        if (newPosition == 0){
            offsetY = -mScrollYCenter
        }

        mLinearLayoutManager.scrollToPositionWithOffset(newPosition,offsetY)
        return mCurTimeUit
    }


    /**
     * recycle播放视频时自动滑动
     *
     * @param time
     */
    fun autoPlayScrolled(time: Int){
        isAutoPlayScrolled().takeIf { it }?.let {
            scrollToTimeTopPosition(time)
        }
    }

    /**
     * 恢复自动滚动的标志
     *
     */
    fun resumeAutoPlayScrolled(){
        mPlayAutoScroll = true
    }

    /**
     * 判断是否自动滚动
     *
     * @return
     */
    fun isAutoPlayScrolled(): Boolean{
        return mPlayAutoScroll
    }

    /**
     * 停止自动滚动
     *
     */
    fun pauseAutoPlayScrolled(){
        mPlayAutoScroll = false
    }
}