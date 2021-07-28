package com.reemii.services.beep;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Build;

import java.io.IOException;

/**
 * Author: yyg
 * Date: 2019-12-23 12:58
 * Description:
 */
public class BeepManager {
    public static final int CANCEL = 1;
    public static final int ON_LINE = 2;
    public static final int OFF_LINE = 3;
    public static final int BACKGROUND = 4;
    public static final int ARRIVED = 5;
    public static final int ORDER = 6;
    public static final int ORDER_PAID = 7;

    private static BeepManager sBeepManager;
    private static MediaPlayer mMediaPlayer;
    private static AssetManager mAssetManager;

    private BeepManager() {

    }

    public void init(Context context) {
        mAssetManager = context.getAssets();
        mMediaPlayer = new MediaPlayer();
    }

    public static BeepManager getInstance() {
        if (sBeepManager == null) {
            synchronized (BeepManager.class) {
                sBeepManager = new BeepManager();
            }
        }
        return sBeepManager;
    }

    public void beep(int type) {
        if (mMediaPlayer == null || mMediaPlayer.isPlaying()) return;
        mMediaPlayer.reset();
        String assetRrs = "";
        switch (type) {
            case 1:
                assetRrs = "order_cancel_tips.mp3";
                break;
            case 2:
                assetRrs = "online_tips.mp3";
                break;
            case 3:
                assetRrs = "offline_tips.mp3";
                break;
            case 4:
                assetRrs = "continue_tips.mp3";
                break;
            case 5:
                assetRrs = "arrived_tips.mp3";
                break;
            case 6:
                assetRrs = "new_order.mp3";
                break;
            case 7:
                assetRrs = "gold_rian.mp3";
                break;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mMediaPlayer.setDataSource(mAssetManager.openFd(assetRrs));
            } else {
                mMediaPlayer.setDataSource(mAssetManager.openFd(assetRrs)
                        .getFileDescriptor(), mAssetManager.openFd(assetRrs)
                        .getStartOffset(), mAssetManager.openFd(assetRrs)
                        .getLength());
            }
            mMediaPlayer.setVolume(0.9f, 0.9f);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        mAssetManager.close();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
