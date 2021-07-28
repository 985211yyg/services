package com.reemii.services.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.RomUtils;

/**
 * Author: yyg
 * Date: 2020-01-02 10:27
 * Description:
 */
public class PermissionHelper {
    public static final int REQUEST_CODE_SERVICE_SMS = 3211;


    public static boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    public static void openNotificationSetting(Context context) {
        Intent intent = new Intent();
        String action = "android.settings.APP_NOTIFICATION_SETTINGS";
        String extraPackage = "android.provider.extra.APP_PACKAGE";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(action);
            intent.putExtra(extraPackage, context.getPackageName());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction(action);
            intent.putExtra("app_package", context.getApplicationContext().getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getApplicationContext().getPackageName()));
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e1) {
            try {
                //应用信息界面
                intent = new Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", context.getPackageName(),
                        null);
                intent.setData(uri);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("", "openNotificationSetting: 失败");
            }
        }
    }


    public static void openSmsService(Activity context) {
        if (RomUtils.isXiaomi()) {
            Intent intent;
            try {
                //MIUI8/9
                intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter",
                        "com.miui.permcenter.permissions.PermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                context.startActivityForResult(intent, REQUEST_CODE_SERVICE_SMS);
            } catch (ActivityNotFoundException e) {
                try {
                    //MIUI5/6
                    intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                    intent.setClassName("com.miui.securitycenter",
                            "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    intent.putExtra("extra_pkgname", context.getPackageName());
                    context.startActivityForResult(intent, REQUEST_CODE_SERVICE_SMS);
                } catch (ActivityNotFoundException e1) {
                    //应用信息界面
                    intent = new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(),
                            null);
                    intent.setData(uri);
                    context.startActivityForResult(intent, REQUEST_CODE_SERVICE_SMS);
                }
            }
        }
    }


    /**
     * 获取自启动管理页面的Intent
     *
     * @param context context
     * @return 返回自启动管理页面的Intent
     */
    public static Intent getAutostartSettingIntent(Context context) {
        ComponentName componentName = null;
        String brand = Build.MANUFACTURER;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (brand.toLowerCase()) {
            case "samsung"://三星
                componentName = new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
                break;
            case "huawei"://华为
                //荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
                componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
//            componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");//目前看是通用的
                break;
            case "xiaomi"://小米
                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
            case "vivo"://VIVO
//            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
                componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                break;
            case "oppo"://OPPO
//            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                componentName = new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
                break;
            case "yulong":
            case "360"://360
                componentName = new ComponentName("com.yulong.android.coolsafe", "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity");
                break;
            case "meizu"://魅族
                componentName = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
                break;
            case "oneplus"://一加
                componentName = new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity");
                break;
            case "letv"://乐视
                intent.setAction("com.letv.android.permissionautoboot");
            default://其他
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                break;
        }
        intent.setComponent(componentName);
        return intent;
    }

}
