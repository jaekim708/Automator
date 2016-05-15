package com.jamjar.automator;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


public class CalAccess {
    private static ArrayList<String> mNameTimeCurr = new ArrayList<>();
    private static ArrayList<String> mNameTimeNext = new ArrayList<>();

    private static Intent muteIntent;
    private static Intent unmuteIntent;
    private static PendingIntent mutePIntent;
    private static PendingIntent unmutePIntent;

    private static AlarmManager manager;


    public static void setup(Context context){
        muteIntent = new Intent(context, ModifyVolume.class);
        muteIntent.putExtra("muteUnmute", "mute");
        mutePIntent = PendingIntent.getBroadcast(context, 0, muteIntent,
                Intent.FILL_IN_DATA);

        unmuteIntent = new Intent(context, ModifyVolume.class);
        unmuteIntent.putExtra("muteUnmute", "unmute");
        unmutePIntent = PendingIntent.getBroadcast(context, 1, unmuteIntent,
                Intent.FILL_IN_DATA);

        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    private static void getCurrEvent(Context context) {
        String selection = "((" + CalendarContract.Instances.BEGIN + " <= ?) AND ("
                + CalendarContract.Instances.END + " >= ?) AND ("
                + CalendarContract.Instances.CALENDAR_ID + " = ?))";

        String[] selectionArgs = new String[] {Long.toString(System.currentTimeMillis()),
                Long.toString(System.currentTimeMillis()), "1"};

        Long startTime = System.currentTimeMillis();
        Long endTime = System.currentTimeMillis() + 604800000;  // 1 week
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/instances/when/" +
                                Long.toString(startTime) + "/" + endTime),
                        new String[]{"title", "begin", "end"}, selection, selectionArgs, "begin");
        cursor.moveToFirst();

        mNameTimeNext.clear();
        for (int i = 0; i < Math.min(cursor.getCount(), 1); i++) {
            mNameTimeNext.add(cursor.getString(0));
            System.out.println("TITLE " + cursor.getString(0));
            System.out.println("START " + cursor.getString(1));
            mNameTimeNext.add(cursor.getString(1));
            mNameTimeNext.add(cursor.getString(2));
            cursor.moveToNext();
        }
    }

    private static void getNextEvent(Context context) {
        String selection = "((" + CalendarContract.Instances.BEGIN + " >= ?) AND ("
                + CalendarContract.Instances.CALENDAR_ID + " = ?))";
        String[] selectionArgs = new String[] {Long.toString(System.currentTimeMillis()), "1"};
        Long startTime = System.currentTimeMillis();
        Long endTime = System.currentTimeMillis() + 604800000;  // 1 week
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/instances/when/" +
                                Long.toString(startTime) + "/" + endTime),
                        new String[]{"title", "begin", "end"}, selection, selectionArgs, "begin");
        cursor.moveToFirst();
        mNameTimeCurr.clear();
        for (int i = 0; i < Math.min(cursor.getCount(), 1); i++) {
            mNameTimeCurr.add(cursor.getString(0));
            System.out.println("TITLE " + cursor.getString(0));
            System.out.println("START " + cursor.getString(1));
            mNameTimeCurr.add(cursor.getString(1));
            mNameTimeCurr.add(cursor.getString(2));
            cursor.moveToNext();
        }
    }
    public static ArrayList<String> getCurr(Context context){
        getCurrEvent(context);
        return mNameTimeCurr;
    }

    public static ArrayList<String> getNext(Context context){
        getNextEvent(context);
        return mNameTimeNext;
    }

    protected static void update(Context context){
        ArrayList<String> curr = CalAccess.getCurr(context);
        ArrayList<String> next = CalAccess.getNext(context);

        if (curr.size() != 0){   // event going on right now
            ModifyVolume.mute(context);
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            formatter.setTimeZone(TimeZone.getDefault());
            long startTime = Long.parseLong(curr.get(1));
            long endTime = Long.parseLong(curr.get(2));

            String start = formatter.format(new Date(startTime));
            String end = formatter.format(new Date(endTime));
            MainActivity.getText().setText("Currently on" +
                    "\nCurrent event is: " + curr.get(0) +
                    "\n Started at " + start + " ending at: " + end);

            manager.set(AlarmManager.RTC_WAKEUP, endTime, unmutePIntent);

        } else {
            if (next.size() != 0){
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                formatter.setTimeZone(TimeZone.getDefault());
                long startTime = Long.parseLong(next.get(1));
                long endTime = Long.parseLong(next.get(2));
                String start = formatter.format(new Date(startTime));
                String end = formatter.format(new Date(endTime));
                MainActivity.getText().setText("Currently on" +
                        "\nNext event is: " + next.get(0) +
                        "\n Starting at " + start + " ending at: " + end);

                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                manager.set(AlarmManager.RTC_WAKEUP, startTime, mutePIntent);
                manager.set(AlarmManager.RTC_WAKEUP, endTime, unmutePIntent);

            }
        }
    }

    public static void cancelAlarms(){
        manager.cancel(mutePIntent);
        manager.cancel(unmutePIntent);
    }
}
