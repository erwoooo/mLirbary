package com.lucky.mod.play.rv.inter

import com.lucky.library.rv.data.TimeEvent

/**
 * @Author cw
 * @Used
 * @Date 2025/2/20 10:45
 */
interface TimeRecycleInterface {

    /**
     * 单个文件播放
     *
     * @param startTime
     */
    fun onTimePlay(startTime: Int)

    /**
     * 滑动到指定位置播放
     *
     * @param startTime
     */
    fun onScrollTimePlay(startTime: Int)

    /**
     * 播放文件列表
     *
     * @param list
     */
    fun onTimeList(list: List<TimeEvent>)
}