package com.lucky.library.rv.inter

import com.lucky.library.rv.data.TimeEvent

/**
 * @Author cw
 * @Used
 * @Date 2025/2/20 10:45
 */
interface TimeRecycleInterface {

    fun onTimePlay(startTime: Int)

    fun onTimeList(list: List<TimeEvent>)
}