package com.reemii.services.bean;

/**
 * Author: yyg
 * Date: 2019-10-27 23:15
 * Description:
 */
public class SpeakEvent {
    SpeakType mType;


    public SpeakType getType() {
        return mType;
    }

    public void setType(SpeakType type) {
        mType = type;
    }

    public SpeakEvent(SpeakType type) {
        mType = type;
    }

    public enum SpeakType {
        order,
        cancel,
        arrived,
        onLine,
        offLine,
        background
    }
}
