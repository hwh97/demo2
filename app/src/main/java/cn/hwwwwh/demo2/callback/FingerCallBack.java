package cn.hwwwwh.demo2.callback;

import android.hardware.fingerprint.FingerprintManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;

/**
 * Created by 97481 on 2017/12/8/ 0008.
 */

public interface FingerCallBack {
    /**
     * 开始识别
     */
    void onStartListening();
    /**
     * 停止识别
     */
    void onStopListening();
    /**
     * 识别成功
     * @param result
     */
    void onSuccess(FingerprintManager.AuthenticationResult result);
    /**
     　* 识别失败
     */
    void onFail(boolean isNormal,String info);
    /**
     * 多次识别失败 的 回调方法
     * @param errorCode
     * @param errString
     */
    void onAuthenticationError(int errorCode, CharSequence errString);
    /**
     　* 识别提示
     */
    void onAuthenticationHelp(int helpCode, CharSequence helpString);
}