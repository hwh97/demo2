package cn.hwwwwh.demo2.view;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.jakewharton.rxbinding2.view.RxView;

import cn.hwwwwh.demo2.R;
import cn.hwwwwh.demo2.callback.FingerCallBack;
import cn.hwwwwh.demo2.utils.ToastUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    Button finger;
    RelativeLayout activityMain;
    Button voice;
    Button imBtn;

    private static final String TAG = "MainActivity";
    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private CancellationSignal mCancellationSignal;
    //回调方法
    private FingerprintManager.AuthenticationCallback mSelfCancelled;
    private FingerCallBack listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        finger = (Button) findViewById(R.id.finger);
        voice=(Button)findViewById(R.id.voice);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,VoiceActivity.class);
                startActivity(intent);
            }
        });
        imBtn=(Button) findViewById(R.id.imBtn);
        imBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (FingerprintManager) getBaseContext().getSystemService(Context.FINGERPRINT_SERVICE);
            mKeyManager = (KeyguardManager) getBaseContext().getSystemService(Context.KEYGUARD_SERVICE);
            mCancellationSignal = new CancellationSignal();
            initSelfCancelled();
        }
        RxView.clicks(finger)
                .subscribeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                            startListening(new FingerCallBack() {
                                @Override
                                public void onStartListening() {
                                    ToastUtils.showToast(getApplicationContext(), "验证开始");
                                }

                                @Override
                                public void onStopListening() {
                                    ToastUtils.showToast(getApplicationContext(), "验证取消");
                                }

                                @Override
                                public void onSuccess(FingerprintManager.AuthenticationResult result) {
                                    ToastUtils.showToast(getApplicationContext(), "验证成功");
                                }

                                @Override
                                public void onFail(boolean isNormal, String info) {
                                    if (isNormal)
                                        ToastUtils.showToast(getApplicationContext(), "指纹验证失败");
                                    else
                                        ToastUtils.showToast(getApplicationContext(), info);
                                }

                                @Override
                                public void onAuthenticationError(int errorCode, CharSequence errString) {
                                    if(errorCode==7)
                                        ToastUtils.showToast(getApplicationContext(), "指纹验证已多次失败，请稍后再试" + errString.toString());
                                }

                                @Override
                                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                                    ToastUtils.showToast(getApplicationContext(), helpString.toString());
                                }
                            });
                        }
                });
    }

    private void initSelfCancelled() {
        mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                // 多次指纹密码验证错误后，进入此方法;并且，不能短时间内调用指纹验证
                listener.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                listener.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                listener.onSuccess(result);
            }

            @Override
            public void onAuthenticationFailed() {
                listener.onFail(true, "识别失败");
            }
        };
    }

    /**
     * 　* 开始监听识别
     */
    public void startListening(FingerCallBack listener) {
        this.listener = listener;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            listener.onFail(false, "版本低于6.0");
            return;
        }
        if (isFinger() == null) {
            listener.onStartListening();
            manager.authenticate(null, mCancellationSignal, 0, mSelfCancelled,
                    null);
        } else {
            listener.onFail(false, isFinger());
        }
    }

    /**
     * 　　* 停止识别
     */
    public void cancelListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            listener.onStopListening();
        }
    }

    /**
     * 同时也少不了各种情况的判断
     * 硬件是否支持
     * 返回null则可以进行指纹识别
     * * 否则返回对应的原因
     *
     * @return
     */
    public String isFinger() {
        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return "没有指纹识别权限";
        }
        //判断硬件是否支持指纹识别
        if (!manager.isHardwareDetected()) {
            return "没有指纹识别模块";
        }
        //判断 是否开启锁屏密码
        if (!mKeyManager.isKeyguardSecure()) {
            return "没有开启锁屏密码";
        }
        //判断是否有指纹录入
        if (!manager.hasEnrolledFingerprints()) {
            return "没有录入指纹";
        }
        return null;
    }

    /**
     * 检查SDK版本
     *
     * @return
     */
    public boolean checkSDKVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }
        return false;
    }

}
