package com.reemii.services.protect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.reemii.services.bean.BeepEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Author: yyg
 * Date: 2019-11-30 16:07
 * Description:
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String TAG = AlarmReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive: 接收到定时广播！");
        EventBus.getDefault().post(new BeepEvent());
    }

}
