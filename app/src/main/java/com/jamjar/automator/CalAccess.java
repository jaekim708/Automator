package com.jamjar.automator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class CalAccess {
    private static PendingIntent mMutePIntent;
    private static PendingIntent mUnmutePIntent;
    private static AlarmManager manager;

    private static final long weekInMillis = 604800000;

    public static void setup(Context context){
        Intent muteIntent = new Intent(context, ModifyVolume.class);
        muteIntent.putExtra("muteUnmute", "mute");
        mMutePIntent = PendingIntent.getBroadcast(context, 0, muteIntent,
                Intent.FILL_IN_DATA);

        Intent unmuteIntent = new Intent(context, ModifyVolume.class);
        unmuteIntent.putExtra("muteUnmute", "unmute");
        mUnmutePIntent = PendingIntent.getBroadcast(context, 1, unmuteIntent,
                Intent.FILL_IN_DATA);

        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    protected static String update(Context context){
        Event currEvent = queryCurrEvent(context);
        String display = "";

        if (currEvent.getName() != null){   // event going on right now
            ModifyVolume.mute(context);
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
            formatter.setTimeZone(TimeZone.getDefault());

            String start = formatter.format(new Date(currEvent.getStart()));
            String end = formatter.format(new Date(currEvent.getEnd()));
            display = context.getString(R.string.currEvent, currEvent.getName(), start, end);

            manager.set(AlarmManager.RTC_WAKEUP, currEvent.getEnd(), mUnmutePIntent);

        } else {
            Event nextEvent = queryNextEvent(context);

            if (nextEvent.getName() != null){
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                formatter.setTimeZone(TimeZone.getDefault());

                String start = formatter.format(new Date(nextEvent.getStart()));
                String end = formatter.format(new Date(nextEvent.getEnd()));
                display = context.getString(R.string.nextEvent, nextEvent.getName(), start, end);

                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                manager.set(AlarmManager.RTC_WAKEUP, nextEvent.getStart(), mMutePIntent);
                manager.set(AlarmManager.RTC_WAKEUP, nextEvent.getEnd(), mUnmutePIntent);

            }
        }
        return display;
    }

    private static Event createEvent(Cursor cursor) {
        Event event = new Event();
        if (cursor != null && cursor.moveToFirst()) {

            if (cursor.getCount() > 0) {
                // just grab the soonest event
                event.setName(cursor.getString(0));
                event.setStart(Long.parseLong(cursor.getString(1)));
                event.setEnd(Long.parseLong(cursor.getString(2)));
            }

            cursor.close();
        }
        return event;
    }

    private static Event queryCurrEvent(Context context) {

        String selection = "((" + CalendarContract.Instances.BEGIN + " <= ?) AND ("
                + CalendarContract.Instances.END + " >= ?) AND ("
                + CalendarContract.Instances.CALENDAR_ID + " = ?))";

        Long queryStartTime = System.currentTimeMillis();
        Long queryEndTime = queryStartTime + weekInMillis;  // only check events in the next week

        String[] selectionArgs = new String[] {Long.toString(queryStartTime),
                Long.toString(queryStartTime), "1"};

        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/instances/when/" +
                                Long.toString(queryStartTime) + "/" + queryEndTime),
                        new String[]{"title", "begin", "end"}, selection, selectionArgs, "begin");

        return createEvent(cursor);
    }

    private static Event queryNextEvent(Context context) {
        String selection = "((" + CalendarContract.Instances.BEGIN + " >= ?) AND ("
                + CalendarContract.Instances.CALENDAR_ID + " = ?))";

        Long queryStartTime = System.currentTimeMillis();
        Long queryEndTime = queryStartTime + weekInMillis;  // only check events in the next week

        String[] selectionArgs = new String[] {Long.toString(queryStartTime), "1"};
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/instances/when/" +
                                Long.toString(queryStartTime) + "/" + queryEndTime),
                        new String[]{"title", "begin", "end"}, selection, selectionArgs, "begin");

        return createEvent(cursor);
    }

    public static void cancelAlarms(){
        manager.cancel(mMutePIntent);
        manager.cancel(mUnmutePIntent);
    }
}

class Event {
    private String name;
    private long startTime;
    private long endTime;

    public Event() {
        name = null;
        startTime = 0;
        endTime = 0;
    }

    public void setName(String n) {
        name = n;
    }

    public void setStart(Long s) {
        startTime = s;
    }

    public void setEnd(Long e) {
        endTime = e;
    }

    public String getName() {
        return name;
    }

    public long getStart(){
        return startTime;
    }

    public long getEnd(){
        return endTime;
    }
}