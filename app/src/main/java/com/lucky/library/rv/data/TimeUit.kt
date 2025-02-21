package com.lucky.library.rv.data

/**
 * @Author cw
 * @Used
 * @Date 2025/2/19 11:45
 */
enum class TimeUit(
    val timeDuration: Int,
    val timeInterval: Int,
    val timeMoveInterval: Int,
    val timeMultiple: Float,
) {


    OneHour(3600, 6, 1, 1f),
    ThirtyMin(1800, 6, 1, 2f),
    TenMin(600, 6, 1, 4f),
    OneMin(60, 6, 1, 8f),
    TwentySecond(20, 6, 1, 16f);

    companion object{
        val orderByDes: Int = 0
        val orderByAsc: Int = 1


        fun getNextTimeUit(curTimeUit: TimeUit, order: Int): TimeUit {
            return if (order == orderByDes) {
                when (curTimeUit) {
                    OneHour -> ThirtyMin
                    ThirtyMin -> TenMin
                    TenMin -> OneMin
                    OneMin, TwentySecond -> TwentySecond
                }
            } else {
                when (curTimeUit) {
                    ThirtyMin, OneHour -> OneHour
                    TenMin -> ThirtyMin
                    OneMin -> TenMin
                    TwentySecond -> OneMin
                }
            }
        }
    }




}