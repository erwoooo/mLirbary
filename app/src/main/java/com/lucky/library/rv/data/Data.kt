package com.lucky.library.rv.data

/**
 * @Author cw
 * @Used 数据类
 * @Date 2025/2/19 11:36
 */

/**
 * 数据类
 *
 * @property startTime  开始时间
 * @property endTime 结束时间
 * @property alarmType 报警类型
 */
 class TimeEvent{
    var startTime: Int = 0
    var endTime: Int = 0
    var alarmType: Int = 0
    override fun toString(): String {
        return "TimeEvent(startTime=$startTime, endTime=$endTime, alarmType=$alarmType)"
    }


}

/**
 * 事件
 *
 * @property startX 开始的X
 * @property startY 开始的Y
 */
data class EventPosition(
    var startX: Int = 0,
    var startY: Int = 0,
    var showPick: Boolean = false,
    var realStartTime: Int = 0,
    var realEndTime: Int = 0,
)