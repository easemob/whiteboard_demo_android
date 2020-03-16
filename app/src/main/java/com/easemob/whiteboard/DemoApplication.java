package com.easemob.whiteboard;

import android.app.Application;
import android.content.Context;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        EMOptions options = new EMOptions();
        options.setAppKey("easemob-demo#chatdemoui");
        EMClient.getInstance().init(this, options);
    }
}
