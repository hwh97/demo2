package cn.hwwwwh.demo2;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.easeui.EaseUI;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by 97481 on 2017/12/8/ 0008.
 */

public class MyApplication extends Application {

    public static MyApplication application = null;

    public static synchronized MyApplication getInstance() {
        return application;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        //讯飞初始化
        SpeechUtility.createUtility(application, SpeechConstant.APPID +"=5a2a157c");
        //环形
        EaseUI.getInstance().init(this, null);
        //EaseUI.getInstance().init(this, null);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
    }

}
