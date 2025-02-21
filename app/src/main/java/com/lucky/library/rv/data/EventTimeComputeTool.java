package com.lucky.library.rv.data;

import android.util.Log;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhaolong on 2017/9/8.
 */

public class EventTimeComputeTool {
    static final String Tag = "EventTimeComputeTool";

    public static List<TimeEvent> findEventTimeList(final int startTime, final int endTime, List<TimeEvent> cameraEventMapList) {
        List<TimeEvent> eventList = new ArrayList<>();
        Log.i(Tag, "findEventTimeList >>> startTime=" + startTime + " >>> endTime=" + endTime);
        int index = Collections.binarySearch(cameraEventMapList, new TimeEvent(), new Comparator<TimeEvent>() {
            @Override
            public int compare(TimeEvent e1, TimeEvent e2) {
                Log.i(Tag, "findEventTimeList >>> compare >>> e1=" + e1.toString());
                if (e1.getEndTime() <= startTime) {
                    return -1;
                } else if (e1.getStartTime() >= endTime) {
                    return 1;
                }
                return 0;
            }

        });
        Log.i(Tag, "findEventTimeList >>> index=" + index);
        if (index >= 0) {
            for (int i = index; i >= 0; i--) {
                int value = addEventTimeToList(eventList, cameraEventMapList.get(i), startTime, endTime);
                Log.i(Tag, "findEventTimeList >>> value1=" + value);
                if (value != 0) {
                    break;
                }
            }
            for (int i = index + 1; i < cameraEventMapList.size(); i++) {
                int value = addEventTimeToList(eventList, cameraEventMapList.get(i), startTime, endTime);

                if (value != 0) {
                    break;
                }
            }
        }
        Log.i(Tag, "findEventTimeList >>> list.size()=" + eventList.size());
        Collections.sort(eventList, new Comparator<TimeEvent>() {
            @Override
            public int compare(TimeEvent o1, TimeEvent o2) {
                if (o1.getStartTime() < o2.getStartTime()) {
                    return -1;
                } else if (o1.getStartTime() > o2.getStartTime()) {
                    return 1;
                }
                return 0;
            }
        });
        return eventList;
    }

    static int addEventTimeToList(List<TimeEvent> eventList, TimeEvent cameraEvent, long startTime, long endTime) {

        if (cameraEvent.getEndTime() <= startTime) {
            return -1;
        } else if (cameraEvent.getStartTime() >= endTime) {
            return 1;
        }

        eventList.add(cameraEvent);
        return 0;
    }


    public static TimeEvent findEvent(List<TimeEvent> list, final long centerLineTime) {
        if (list != null) {
            int index = Collections.binarySearch(list, new TimeEvent(), new Comparator<TimeEvent>() {
                @Override
                public int compare(TimeEvent e1, TimeEvent e2) {
                    if (e1.getEndTime() <= centerLineTime) {
                        return -1;
                    } else if (e1.getStartTime() > centerLineTime) {
                        return 1;
                    }
                    return 0;
                }
            });
            if (index >= 0) {
                return list.get(index);
            }
        }
        return null;
    }


    //传过来的时间参数有可能在list中不存在，需要从list中获取到getStartTime刚好大于centerLineTime的CameraEvent
    public static TimeEvent findNearEvent(List<TimeEvent> list, final long centerLineTime) {
        if (list != null) {
            TimeEvent event = null;
            for (TimeEvent cameraEvent : list) {
                if(cameraEvent.getStartTime()>centerLineTime){
                    return cameraEvent;
                }
                event = cameraEvent;
            }
            return event;
        }
        return null;
    }
}
