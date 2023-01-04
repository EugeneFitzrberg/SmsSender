/*
 * Автор сия безобразия - github.com/EugeneFitzrberg/SmsSender
 *
 * */

package com.example.sms_watcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MessageDetails extends Dialog {

    public Button closeMessDetailBtn;
    public Activity c;
    public TextView messageView;
    public TextView abonentView;

    public MessageDetails(Context context) {
        super(context);
        this.c = (Activity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.message_details);
        messageView = findViewById(R.id.smsTextMess);
        abonentView = findViewById(R.id.abonent);
        closeMessDetailBtn = (Button) findViewById(R.id.closeMessDetailBTN);

    }

}
