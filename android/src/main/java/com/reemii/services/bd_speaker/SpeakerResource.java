package com.reemii.services.bd_speaker;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;


/**
 * 文件拷贝
 */

public class SpeakerResource {
    public static final String TAG = SpeakerResource.class.getSimpleName();
    private static final String VOICE_FEMALE = "F";
    private static final String VOICE_MALE = "M";
    private static final String VOICE_DUYY = "Y";
    private static final String VOICE_DUXY = "X";
    private static final String TEXT_MODEL = "bd_etts_text.dat";
    private static final String VOICE_MALE_MODEL = "bd_etts_common_speech_m15_mand_eng_high_am-mgc_v3.6.0_20190117.dat";
    private static final String VOICE_FEMALE_MODEL = "bd_etts_common_speech_f7_mand_eng_high_am-mgc_v3.6.0_20190117.dat";
    private static final String VOICE_DUXY_MODEL = "bd_etts_common_speech_yyjw_mand_eng_high_am-mgc_v3.6.0_20190117.dat";
    private static final String VOICE_DUYY_MODEL = "bd_etts_common_speech_as_mand_eng_high_am-mgc_v3.6.0_20190117.dat";
    private AssetManager assets;
    private String destPath;
    private String textFilename;
    private String modelFilename;
    private static HashMap<String, Boolean> mapInitied = new HashMap<String, Boolean>();
    private static SpeakerResource sSpeakerResource;

    private SpeakerResource() {
    }

    public static SpeakerResource getInstance() {
        if (sSpeakerResource == null) {
            synchronized (SpeakerResource.class) {
                if (sSpeakerResource == null) {
                    sSpeakerResource = new SpeakerResource();
                }
            }
        }
        return sSpeakerResource;

    }


    public void init(Context context) {
        this.assets = context.getAssets();
        this.destPath = FileUtil.createTmpDir(context);
        try {
            setOfflineVoiceType(VOICE_FEMALE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getModelFilename() {
        return modelFilename;
    }

    public String getTextFilename() {
        return textFilename;
    }

    public void setOfflineVoiceType(String voiceType) throws IOException {
        String text = TEXT_MODEL;
        String model;
        if (VOICE_MALE.equals(voiceType)) {
            model = VOICE_MALE_MODEL;
        } else if (VOICE_FEMALE.equals(voiceType)) {
            model = VOICE_FEMALE_MODEL;
        } else if (VOICE_DUXY.equals(voiceType)) {
            model = VOICE_DUXY_MODEL;
        } else if (VOICE_DUYY.equals(voiceType)) {
            model = VOICE_DUYY_MODEL;
        } else {
            throw new RuntimeException("voice type is not in list");
        }
        textFilename = copyAssetsFile(text);
        modelFilename = copyAssetsFile(model);
    }


    private String copyAssetsFile(String sourceFilename) throws IOException {
        String destFilename = destPath + "/" + sourceFilename;
        boolean recover = false;
        Boolean existed = mapInitied.get(sourceFilename); // 启动时完全覆盖一次
        if (existed == null || !existed) {
            recover = true;
        }
        FileUtil.copyFromAssets(assets, sourceFilename, destFilename, recover);
        Log.e(TAG, "文件复制成功：" + destFilename);
        return destFilename;
    }


}
