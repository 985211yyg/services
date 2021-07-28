package com.reemii.services.protect;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.reemii.services.R;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;


/**
 * Created by yyg on 2018/12/4 ,13:48
 */
public class ProtectService extends Service {

    private final static String TAG = ProtectService.class.getSimpleName();
    public static final String CHANNEL_ID = "guard";
    public static final int NOTIFICATION_ID = 1234;
    public static final int BROADCAST_CODE = 12345;
    private MediaPlayer mMediaPlayer;

    public ProtectService() {
        Log.e(TAG, "ProtectService: ");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = MediaPlayer.create(this, R.raw.music);
        mMediaPlayer.setVolume(0.0f, 0.0f);
        mMediaPlayer.setLooping(true);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //循环播放音乐
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPlayMusic();
            }
        }).start();
        init();
        return START_STICKY;
    }

    private void init() {
        //开启定时
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, BROADCAST_CODE,
                new Intent(this, AlarmReceiver.class), 0);
//        //android 6.0以上  3分中一次心跳 进入后台变成休眠模式后9分钟调用一次
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    SystemClock.elapsedRealtime() + 300 * 1000, pendingIntent);
//        } else {
//            //6.0一下 3分中一次心跳
//            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    SystemClock.elapsedRealtime(), 300 * 1000, pendingIntent);
//        }

        //6.0一下 5分中一次心跳
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), 360 * 1000, pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground();
        }
    }

    //8.0及以上系统的处理  1、建立NotificationChannel  2.创建和建立NotificationChannel共同CHANNEL_ID的Notification   3、显示
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForeground() {
        //设定的通知渠道名称
        String channelName = this.getClass().getSimpleName();
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription("后台保护");
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.logo) //设置通知图标
                .setContentTitle("正在后台服务，请勿强行停止！")//设置通知标题
                .setContentText("熊猫代驾")//设置通知内容
                .setAutoCancel(false) //用户触摸时，自动关闭
                .setOngoing(true)//设置处于运行状态
                .setPriority(PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE);

        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID, builder.build());

    }


    private void startPlayMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    private void stopPlayMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayMusic();
        stopForeground(true);
        stopSelf();

    }


}

