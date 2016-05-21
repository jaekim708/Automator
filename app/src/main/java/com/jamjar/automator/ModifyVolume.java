package com.jamjar.automator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
/**
 * Created by jae on 4/26/16.
 */
public class ModifyVolume extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Alarm received");
        if(intent.getStringExtra("muteUnmute").equals("mute")) {
            mute(context);
        }
        else if (intent.getStringExtra("muteUnmute").equals("unmute")) {
            unmute(context);
        }
    }

    public static void mute(Context context) {
        AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    public static void unmute(Context context) {
        AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        CalAccess.update(context);

    }
}

