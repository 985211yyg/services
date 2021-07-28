package com.reemii.services.bd_speaker;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类是对SpeechSynthesizer的封装
 * <p>
 * Created by fujiayi on 2017/5/24.
 */

public class SpeakerManager implements SpeechSynthesizerListener {
    private static final String TAG = SpeakerManager.class.getSimpleName();
    private static final String KEY_SN = "KEY_SpeakerManager_SN";

    private static final int INIT = 1;
    private static final int RELEASE = 11;

    private SpeechSynthesizer mSpeechSynthesizer;
    private Context context;
    private HandlerThread hThread;
    private Handler tHandler;
    boolean isInitSuccess = false;

    public SpeakerManager(Context context) {
        LogUtils.eTag(TAG, "constructor");
        this.context = context;
        SpeakerResource.getInstance().init(context);
        initThread();
        String cachedSN = MMKV.defaultMMKV().decodeString(KEY_SN);
        if (cachedSN != null) {
            LogUtils.eTag(TAG, "has cached sn = " + cachedSN);
            config(cachedSN);
        }
    }

    public void config(String sn) {
        LogUtils.eTag(TAG, "config sn = " + sn);
        if (mSpeechSynthesizer == null && !isInitSuccess) {
            MMKV.defaultMMKV().encode(KEY_SN, sn);
            runInHandlerThread(INIT);
        }
    }

    /**
     * 合成并播放
     *
     * @param text 小于1024 GBK字节，即512个汉字或者字母数字
     * @return =0表示成功
     */
    public int speak(String text) {
        if (mSpeechSynthesizer != null && isInitSuccess) {
            return mSpeechSynthesizer.speak(text);
        }
        return -1;
    }

    public void release() {
        LogUtils.eTag(TAG, "release");
        runInHandlerThread(RELEASE);
    }

    /**
     * 注意该方法需要在新线程中调用。且该线程不能结束。详细请参见NonBlockSyntherizer的实现
     *
     * @return 是否初始化成功
     */
    protected boolean init() {
        Log.e(TAG, "初始化开始");
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(context);
        mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        //todo 2021-07-21 22:11:07 替换
        mSpeechSynthesizer.setAppId("24573700");
        mSpeechSynthesizer.setApiKey("0oaWpOl1Gr3VfKOaoMpoboep", "sc9HhAsCv79B5wXrIW82dKg3g4XxF0hl");
        setParams(getParams());
        // 初始化tts
        // 改为混合模式 离线在线 同时激活 当离线模式无法使用时候切换在线语音模式
        int result = mSpeechSynthesizer.initTts(TtsMode.MIX);
        if (result != 0 && result != -204) {
            Log.e(TAG, "【error】initTts 初始化失败 + errorCode：" + result);
            ToastUtils.showLong("initTts 初始化失败 + errorCode：" + result);
            return false;
        }
        Log.e(TAG, "代码：" + result);
//        ToastUtils.showShort("代码：" + result);
        return true;
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");
//        params.put(SpeechSynthesizer.PARAM_AUTH_SN, "9d8ee6a0-7c35ca45-0216-0045-21631");//小米
        params.put(SpeechSynthesizer.PARAM_AUTH_SN, MMKV.defaultMMKV().decodeString(KEY_SN));//小米
//        params.put(SpeechSynthesizer.PARAM_AUTH_SN, "98834044-80db53ca-0216-00b2-2162e");//三星
//        params.put(SpeechSynthesizer.PARAM_AUTH_SN, "9448714f-74309b0d-0216-0088-2162f");//荣耀
//        params.put(SpeechSynthesizer.PARAM_AUTH_SN, "7ff9a22b-743e190c-0216-0013-21630");//锤子

        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, SpeakerResource.getInstance().getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                SpeakerResource.getInstance().getModelFilename());
        return params;
    }


    /**
     * 合成并播放
     *
     * @param text        小于1024 GBK字节，即512个汉字或者字母数字
     * @param utteranceId 用于listener的回调，默认"0"
     * @return =0表示成功
     */
    public int speak(String text, String utteranceId) {
        return mSpeechSynthesizer.speak(text, utteranceId);
    }

    /**
     * 只合成不播放
     *
     * @param text 合成的文本
     * @return =0表示成功
     */
    public int synthesize(String text) {
        return mSpeechSynthesizer.synthesize(text);
    }

    public int synthesize(String text, String utteranceId) {

        return mSpeechSynthesizer.synthesize(text, utteranceId);
    }

    public int batchSpeak(List<Pair<String, String>> texts) {
        List<SpeechSynthesizeBag> bags = new ArrayList<SpeechSynthesizeBag>();
        for (Pair<String, String> pair : texts) {
            SpeechSynthesizeBag speechSynthesizeBag = new SpeechSynthesizeBag();
            speechSynthesizeBag.setText(pair.first);
            if (pair.second != null) {
                speechSynthesizeBag.setUtteranceId(pair.second);
            }
            bags.add(speechSynthesizeBag);

        }
        return mSpeechSynthesizer.batchSpeak(bags);
    }

    public void setParams(Map<String, String> params) {
        if (params != null) {
            for (Map.Entry<String, String> e : params.entrySet()) {
                mSpeechSynthesizer.setParam(e.getKey(), e.getValue());
            }
        }
    }

    public int pause() {
        return mSpeechSynthesizer.pause();
    }

    public int resume() {
        return mSpeechSynthesizer.resume();
    }

    public int stop() {
        if (mSpeechSynthesizer != null && isInitSuccess) {
            return mSpeechSynthesizer.stop();
        }
        return -1;
    }

    /**
     * 引擎在合成时该方法不能调用！！！
     * 注意 只有 TtsMode.MIX 才可以切换离线发音
     *
     * @return
     */
    public int loadModel(String modelFilename, String textFilename) {
        int res = mSpeechSynthesizer.loadModel(modelFilename, textFilename);
        return res;
    }

    /**
     * 设置播放音量，默认已经是最大声音
     * 0.0f为最小音量，1.0f为最大音量
     *
     * @param leftVolume  [0-1] 默认1.0f
     * @param rightVolume [0-1] 默认1.0f
     */
    public void setStereoVolume(float leftVolume, float rightVolume) {
        mSpeechSynthesizer.setStereoVolume(leftVolume, rightVolume);
    }

    private void doRelease() {
        if (mSpeechSynthesizer == null) return;
        mSpeechSynthesizer.stop();
        mSpeechSynthesizer.release();
        mSpeechSynthesizer = null;
        isInitSuccess = false;
        hThread.quitSafely();
        LogUtils.eTag(TAG, "release done");
    }

    @Override
    public void onSynthesizeStart(String s) {
        Log.e(TAG, "onSynthesizeStart: " + s);

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i, int i1) {
        Log.e(TAG, "onSynthesizeDataArrived: " + s);

    }

    @Override
    public void onSynthesizeFinish(String s) {
        Log.e(TAG, "onSynthesizeFinish: " + s);

    }

    @Override
    public void onSpeechStart(String s) {
        Log.e(TAG, "onSpeechStart: " + s);

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        Log.e(TAG, "onSpeechProgressChanged: " + s);

    }

    @Override
    public void onSpeechFinish(String s) {
        Log.e(TAG, "onSpeechFinish: " + s);

    }

    @Override
    public void onError(String s, SpeechError speechError) {
        Log.e(TAG, "onError: " + s + "|" + speechError.toString());
    }


    private void initThread() {
        hThread = new HandlerThread("SpeakerManagerPro-thread");
        hThread.start();

        tHandler = new Handler(hThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case INIT:
                        isInitSuccess = init();
                        if (isInitSuccess) {
                            Log.e(TAG, "初始化成功");
                        } else {
                            Log.e(TAG, "合成引擎初始化失败, 请查看日志");
                        }
                        break;
                    case RELEASE:
                        doRelease();
                        break;
                    default:
                        break;
                }

            }
        };
    }


    private void runInHandlerThread(int action) {
        runInHandlerThread(action, null);
    }

    private void runInHandlerThread(int action, Object obj) {
        Message msg = Message.obtain();
        msg.what = action;
        msg.obj = obj;
        tHandler.sendMessage(msg);
    }
}
