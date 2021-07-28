package com.reemii.services;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lxj.xpopup.core.CenterPopupView;

/**
 * Author: yyg
 * Date: 2019-12-21 21:40
 * Description:
 */
public class ServiceDialog extends CenterPopupView {
    private TextView cancel, sure;
    private Callback mCallback;

    public ServiceDialog(@NonNull Context context,Callback callback) {
        super(context);
        mCallback = callback;
    }

    /**
     * 具体实现的类的布局
     *
     * @return
     */
    @Override
    protected int getImplLayoutId() {
        return R.layout.service_dialog_view;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        cancel = findViewById(R.id.service_dialog_cancel);
        sure = findViewById(R.id.service_dialog_sure);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onCancel();
                dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSure();
                dismiss();
            }
        });
    }


    public ServiceDialog setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    public interface Callback {
        void onSure();

        void onCancel();
    }


}
