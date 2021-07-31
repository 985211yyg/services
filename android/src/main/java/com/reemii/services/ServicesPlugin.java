package com.reemii.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.NetworkUtils.NetworkType;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.reemii.services.bd_speaker.SpeakerManager;
import com.reemii.services.bean.BeepEvent;
import com.reemii.services.bean.SpeakerSN;
import com.reemii.services.beep.BeepManager;
import com.reemii.services.protect.ProtectService;
import com.reemii.services.utils.DeviceIdUtil;
import com.reemii.services.utils.GpsUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Date;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rxhttp.wrapper.param.RxHttp;

import static android.content.Context.POWER_SERVICE;

/**
 * ServicesPlugin
 */
public class ServicesPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    public static final String TAG = ServicesPlugin.class.getSimpleName();
    public static final String SERVICE_PLUGIN = "com.reemii.ServicesPlugin";
    public static final String BEEP_ORDER_PAID = "order_paid";
    public static final String INIT = "init";
    public static final String SPEAKER = "speaker";
    public static final String STOP_SPEAKER = "stop_speaker";
    public static final String UPDATE = "update";
    public static final String DOMAIN = "domain";
    private MethodChannel mMethodChannel;
    private ActivityPluginBinding mActivityPluginBinding;
    private FlutterPluginBinding mFlutterPluginBinding;
    private UpdateConfiguration configuration = new UpdateConfiguration()
            //输出错误日志
            .setEnableLog(true)
            //设置自定义的下载
//            .setHttpManager()
            //下载完成自动跳动安装页面
            .setJumpInstallPage(true)
            //设置对话框背景图片 (图片规范参照demo中的示例图)
            //.setDialogImage(R.drawable.ic_dialog)
            //设置按钮的颜色
//            .setDialogButtonColor(Color.parseColor("#FF5DA0D7"))
            //设置对话框强制更新时进度条和文字的颜色
            //.setDialogProgressBarColor(Color.parseColor("#E743DA"))
            //设置按钮的文字颜色
            .setDialogButtonTextColor(Color.WHITE)
            //设置是否显示通知栏进度
            .setShowNotification(true)
            //设置是否提示后台下载toast
            .setShowBgdToast(true)
            //设置强制更新
//            .setForcedUpgrade(false)
            //设置对话框按钮的点击监听
            .setButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onButtonClick(int id) {

                }
            })
            //设置下载过程的监听
            .setOnDownloadListener(new OnDownloadListener() {
                @Override
                public void start() {

                }

                @Override
                public void downloading(int max, int progress) {

                }

                @Override
                public void done(File apk) {

                }

                @Override
                public void cancel() {

                }

                @Override
                public void error(Exception e) {

                }
            });

    private SpeakerManager mSpeakerManager;


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            //初始化  语音、电量白名单、保活进程
            case INIT:
                String token = (String) call.arguments;
                SPUtils.getInstance().put("token", token);
                init();
                break;
            case BEEP_ORDER_PAID:
                // 付款
                BeepManager.getInstance().beep(BeepManager.ORDER_PAID);
                break;
            case SPEAKER:
                // 说话
                speaker((String) call.arguments);
                break;
            case STOP_SPEAKER:
                // 停止说话
                stopSpeaker();
                break;
            case UPDATE:
                // 获取版本信息 build versionName url desc
                Map<String, Object> updateModel = (Map<String, Object>) call.arguments;
                checkUpdate(updateModel);
                break;
            case DOMAIN:
                // 设置环境
                SPUtils.getInstance().put("domain", (String) call.arguments);
                break;
            case "exit":
                // 设置环境
                AppUtils.exitApp();
                break;
            case "setting":
                if (mActivityPluginBinding == null) break;
                // 打开安卓的引导设置
                SettingBottomPopup.show(mActivityPluginBinding.getActivity(), new SettingBottomPopup.SettingCallback() {
                    @Override
                    public void settingItem(SettingBottomPopup.SettingType settingType) {
                    }

                    @Override
                    public void onComplete() {
                        SPUtils.getInstance().put(SettingBottomPopup.APP_SETTING, 1);
                    }
                });

                break;
            default:
                result.notImplemented();
                break;
        }
    }

    /*
        "url": updateModel.data.currentUrl,
        "versionName": updateModel.data.currentVersion,
        "description": updateModel.data.contents,
        "isForce": updateModel.data.isEnforce,
        "code": 1111,
     */
    private void checkUpdate(Map<String, Object> updateModel) {
        if (mActivityPluginBinding == null) return;
        //是否强制更新
        configuration.setForcedUpgrade((int) updateModel.get("isForce") != 0);
        DownloadManager.getInstance(mActivityPluginBinding.getActivity())
                .setApkName(new Date().getTime() + "熊猫代驾.apk")
                .setApkUrl((String) updateModel.get("url"))
                .setSmallIcon(R.drawable.logo)
                .setShowNewerToast(true)
                .setConfiguration(configuration)
                .setApkVersionCode(1111)
                .setApkVersionName((String) updateModel.get("versionName"))
                .setApkDescription((String) updateModel.get("description"))
                .download();
    }


    //语音播报
    private void speaker(String arguments) {
        Log.e(TAG, "speaker: " + arguments);
        if (arguments.isEmpty()) return;
        mSpeakerManager.speak(arguments);
    }

    //语音播报
    private void stopSpeaker() {
        mSpeakerManager.stop();
    }


    //保活进行
    private void init() {
        if (this.mActivityPluginBinding != null) {
            //请求电量白名单
            whiteListSetting(mActivityPluginBinding.getActivity());
            //获取百度离线语音的序列号
            RxHttp.postJson(SPUtils.getInstance().getString("domain")
                    .concat("/staff/staff/baiduVoiceSdk?token=")
                    .concat(SPUtils.getInstance().getString("token")))
                    .add("device_id", DeviceIdUtil.getDeviceId(mActivityPluginBinding.getActivity()))
                    .asObject(SpeakerSN.class)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<SpeakerSN>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(SpeakerSN speakerSN) {
                            if (speakerSN.getData().getResult().isEmpty()) {
                                ToastUtils.showLong("激活离线语音失败，请重启应用或者联系技术！");
                            } else {
                                //使用设备id获取离线语音序列号
                                mSpeakerManager.config(speakerSN.getData().getResult());
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, "onError: " + e.toString());

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

        if (mFlutterPluginBinding != null) {
            // 0. 开起保活进程
            Intent protectServiceIntent = new Intent(mFlutterPluginBinding.getApplicationContext(), ProtectService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mFlutterPluginBinding.getApplicationContext().startForegroundService(protectServiceIntent);
            } else {
                mFlutterPluginBinding.getApplicationContext().startService(protectServiceIntent);
            }
        }

    }

    //添加到Engine
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        Log.e(TAG, "onAttachedToEngine: ");
        mFlutterPluginBinding = binding;
        mMethodChannel = new MethodChannel(binding.getBinaryMessenger(), SERVICE_PLUGIN);
        mMethodChannel.setMethodCallHandler(this);
        // 1. Init Beep
        BeepManager.getInstance().init(binding.getApplicationContext());
        // 2. 接收后台接单提示
        EventBus.getDefault().register(this);
        // 3. 注册检查网络
        NetworkUtils.registerNetworkStatusChangedListener(mNetworkStatusChangedListener);
        // 4. 注册GPS监控
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        binding.getApplicationContext().registerReceiver(mBroadcastReceiver, intentFilter);
        // 5. 初始化语音播报类
        mSpeakerManager = new SpeakerManager(binding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        Log.e(TAG, "onDetachedFromEngine: ");
        // 0. 取消保活进程
        Intent protectServiceIntent = new Intent(binding.getApplicationContext(), ProtectService.class);
        binding.getApplicationContext().stopService(protectServiceIntent);
        // 1. Cancel Beep
        BeepManager.getInstance().destroy();
        // 2. 取消接收后台接单提示
        EventBus.getDefault().unregister(this);
        // 3. 取消注册检查网络
        NetworkUtils.unregisterNetworkStatusChangedListener(mNetworkStatusChangedListener);
        // 4. 取消注册GPS监控
        binding.getApplicationContext().unregisterReceiver(mBroadcastReceiver);
        // 5. 释放引擎
        mSpeakerManager.release();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBackgroundBeep(BeepEvent event) {
        mMethodChannel.invokeMethod("backgroundBeep", null);
    }

    private void whiteListSetting(Activity activity) {
        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到电量优化白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final String packageName = activity.getPackageName();
            PowerManager manager = (PowerManager) activity.getSystemService(POWER_SERVICE);
            boolean isIgnoring = manager.isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent batteryIgnoreIntent = new Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                batteryIgnoreIntent.setData(Uri.parse("package:" + packageName));
                new XPopup.Builder(activity).asCustom(new ServiceDialog(activity, new ServiceDialog.Callback() {
                    @Override
                    public void onSure() {
                        activity.startActivity(batteryIgnoreIntent);
                    }

                    @Override
                    public void onCancel() {
                        ToastUtils.showShort("请将应用添加到电量白名单中，否则可能影响接单！");
                    }
                })).show();
            }
        }
    }

    MyGpsReceive mBroadcastReceiver = new MyGpsReceive();

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.mActivityPluginBinding = binding;
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        this.mActivityPluginBinding = null;
    }

    //gps监听
    class MyGpsReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!new GpsUtil().isOPen(context)) {
                mSpeakerManager.speak("当前定位服务不可用，请检查您的设置！");
                ToastUtils.showLong("当前定位服务不可用，请检查您的设置！");
            }
        }
    }

    private NetworkStatusChangedListener mNetworkStatusChangedListener = new NetworkStatusChangedListener();

    //网络变化监听
    class NetworkStatusChangedListener implements NetworkUtils.OnNetworkStatusChangedListener {

        @Override
        public void onDisconnected() {
            ToastUtils.showLong("当前网络不可用！");
            mSpeakerManager.speak("当前网络不可用！当前网络不可用！");
        }

        @Override
        public void onConnected(NetworkType networkType) {
            String text = null;
            switch (networkType) {
                case NETWORK_2G:
                case NETWORK_3G: {
                    ToastUtils.showLong("当前网络较差！");
                    text = "当前网络较差！";
                    break;
                }
                case NETWORK_4G: {
                    ToastUtils.showLong("连接到4G网络！");
                    break;
                }
                case NETWORK_WIFI: {
                    ToastUtils.showLong("已连接到无线网络！");
                    break;
                }
                case NETWORK_UNKNOWN: {
                    ToastUtils.showLong("当前网络未知！");
                    text = "当前网络较差！";
                    break;
                }
                case NETWORK_NO: {
                    ToastUtils.showLong("当前网络不可用！");
                    text = "当前网络不可用！";
                    break;
                }
                default: {
                    // 暂不处理
                }
            }
            if (text != null) {
                mSpeakerManager.speak(text);
            }
        }
    }
}
