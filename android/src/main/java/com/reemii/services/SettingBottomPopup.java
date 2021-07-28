package com.reemii.services;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.reemii.services.utils.PermissionHelper;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.POWER_SERVICE;

/**
 * Author: yyg
 * Date: 2020-01-03 15:49
 * Description:
 */
public class SettingBottomPopup extends BottomPopupView {
    public static final String APP_SETTING = "app_setting";
    public static final String TAG = SettingBottomPopup.class.getSimpleName();
    private Activity mContext;
    private TextView battery, sms, back, notification, setted, complete;
    private SettingCallback mSettingCallback;
    private int count;

    public SettingBottomPopup(@NonNull Activity context, SettingCallback callback) {
        super(context);
        mContext = context;
        mSettingCallback = callback;
        count = 0;
    }

    /**
     * 具体实现的类的布局
     *
     * @return
     */
    @Override
    protected int getImplLayoutId() {
        return R.layout.service_setting_popup;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        initView();
        initListener();
    }

    private void initListener() {
        setted.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SPUtils.getInstance().put("app_setting", 1);
                dismiss();
            }
        });
        complete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                complete.setText("继续");
                if (count > 3) {
                    dismiss();
                }
                switch (count) {
                    case 0:
                        batterySetting();
                        mSettingCallback.settingItem(SettingType.battery);
                        battery.setText(R.string.battery_1);
                        battery.setTextColor(mContext.getResources().getColor(R.color.themeColor));
                        complete.setText("继续");
                        break;
                    case 1:
                        smsSetting();
                        mSettingCallback.settingItem(SettingType.sms);
                        sms.setText(R.string.sms_1);
                        sms.setTextColor(mContext.getResources().getColor(R.color.themeColor));
                        complete.setText("继续");

                        break;
                    case 2:
                        backSetting();
                        mSettingCallback.settingItem(SettingType.back);
                        back.setText(R.string.back_1);
                        back.setTextColor(mContext.getResources().getColor(R.color.themeColor));
                        complete.setText("继续");

                        break;
                    case 3:
                        notificationtSetting();
                        mSettingCallback.settingItem(SettingType.notification);
                        notification.setText(R.string.notificatin_1);
                        notification.setTextColor(mContext.getResources().getColor(R.color.themeColor));
                        complete.setText("点击关闭");
                        mSettingCallback.onComplete();
                        break;
                }
                count += 1;

            }
        });


    }

    private void initView() {
        battery = findViewById(R.id.setting_battery);
        back = findViewById(R.id.setting_back);
        sms = findViewById(R.id.setting_sms);
        notification = findViewById(R.id.setting_notification);
        setted = findViewById(R.id.setting_setted);
        complete = findViewById(R.id.setting_all);
    }


    public static void show(final Activity context, final SettingCallback callback) {
        //已经设置过了
        if (SPUtils.getInstance().getInt(APP_SETTING, 0) == 1) return;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                new XPopup.Builder(context)
                        .dismissOnBackPressed(false)
                        .dismissOnTouchOutside(false)
                        .enableDrag(false)
                        .asCustom(new SettingBottomPopup(context, callback))
                        .show();
            }
        }, 1500);


    }


    private void batterySetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String packageName = mContext.getPackageName();
            PowerManager manager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
            boolean isIgnoring = manager.isIgnoringBatteryOptimizations(packageName);
            Intent batteryIgnoreIntent = new Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            batteryIgnoreIntent.setData(Uri.parse("package:" + packageName));
            if (!isIgnoring) {
                try {
                    batteryIgnoreIntent.setData(Uri.parse("package:" + packageName));
                    mContext.startActivity(batteryIgnoreIntent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void smsSetting() {
        if (PermissionUtils.isGranted(PermissionConstants.SMS)) {
            PermissionHelper.openSmsService(mContext);
        } else {
            PermissionUtils.permission(PermissionConstants.SMS).callback(new PermissionUtils.SimpleCallback() {
                @Override
                public void onGranted() {
                    //针对小米手机
                    PermissionHelper.openSmsService(mContext);
                }

                @Override
                public void onDenied() {
                    smsSetting();
                }
            }).request();
        }

    }

    private void backSetting() {
        Intent intent = PermissionHelper.getAutostartSettingIntent(mContext);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.e("activity", "没有注册");
            }
        }
    }

    private void notificationtSetting() {
        PermissionHelper.openNotificationSetting(mContext);
    }


    public interface SettingCallback {
        void settingItem(SettingType settingType);

        void onComplete();

    }

    public enum SettingType {
        battery,
        sms,
        notification,
        back

    }


}
