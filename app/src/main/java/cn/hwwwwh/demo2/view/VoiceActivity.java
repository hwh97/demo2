package cn.hwwwwh.demo2.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import cn.hwwwwh.demo2.R;
import cn.hwwwwh.demo2.utils.FucUtil;
import cn.hwwwwh.demo2.utils.JsonParser;
import cn.hwwwwh.demo2.utils.ToastUtils;

public class VoiceActivity extends AppCompatActivity implements View.OnClickListener {
    //voice
    private static String TAG = VoiceActivity.class.getSimpleName();
    // 语音识别对象
    private SpeechRecognizer mAsr;
    private Toast mToast;
    // 缓存
    private SharedPreferences mSharedPreferences;
    // 云端语法文件
    private String mCloudGrammar = null;
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String , String>();
    private static final int REQUESTCODE = 10010;

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(VoiceActivity.this, mInitListener);
        mCloudGrammar = FucUtil.readFile(this,"grammar_sample.abnf","utf-8");
        mSharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        initLayout();
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                ToastUtils.showToast(getApplicationContext(),"初始化失败,错误码："+code);
            }
        }
    };

    /**
     * 初始化Layout。
     */
    private void initLayout() {
        findViewById(R.id.btn_startspeech).setOnClickListener(this);
        webView=(WebView) findViewById(R.id.web_voice);
        // 设置WebView的客户端
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;// 返回false
            }
        });
        webView.loadUrl("http://www.baidu.com/");
    }

    @Override
    public void onClick(View v) {
        if (!permission()) {
            AskForPermission();
            ToastUtils.showToast(VoiceActivity.this, "请同意麦克风权限");
            return;
        }
        if( null == mAsr ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
             ToastUtils.showToast(getApplicationContext(), "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化" );
            return;
        }

        if(null == mEngineType) {
            ToastUtils.showToast(getApplicationContext(),"请先选择识别引擎类型");
            return;
        }
        switch (v.getId()) {
            case R.id.btn_startspeech:
                startSpeechDialog();
                break;
        }
    }
    /**
     * 创建Dialog
     */
    private void startSpeechDialog() {
        //1. 创建RecognizerDialog对象
        RecognizerDialog mDialog = new RecognizerDialog(this, new MyInitListener()) ;
        //2. 设置accent、 language等参数
        mDialog.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
        mDialog.setParameter(SpeechConstant. ACCENT, "mandarin" );
        // 若要将UI控件用于语义理解，必须添加以下参数设置，设置之后 onResult回调返回将是语义理解
        // 结果
        //设置是否显示标点0表示不显示，1表示显示
         mDialog.setParameter(SpeechConstant.ASR_PTT, "0");
        // mDialog.setParameter("asr_sch", "1");
        // mDialog.setParameter("nlp_version", "2.0");
        //3.设置回调接口
        mDialog.setListener( new MyRecognizerDialogListener()) ;
        //4. 显示dialog，接收语音输入
        mDialog.show() ;
    }

    /**
     * 申请权限
     */
    private boolean permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUESTCODE);
            return false;
        }else
            return true;
    }
    /**
     * 请求申请权限
     */
    private void AskForPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请同意麦克风权限!");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("转去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
                startActivity(intent);
            }
        });
        builder.create().show();
    }
    /**
     * Dialog监听回调
     */
    class MyRecognizerDialogListener implements RecognizerDialogListener {

        /**
         * @param results
         * @param isLast  是否说完了
         */
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String result = results.getResultString(); //为解析的
            //showTip(result) ;
            System. out.println(" 没有解析的 :" + result);

            String text = JsonParser.parseIatResult(result) ;//解析过后的
            System. out.println(" 解析后的 :" + text);

            String sn = null;
            // 读取json结果中的 sn字段
            try {
                JSONObject resultJson = new JSONObject(results.getResultString()) ;
                sn = resultJson.optString("sn" );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mIatResults.put(sn, text) ;//没有得到一句，添加到

            StringBuffer resultBuffer = new StringBuffer();
            for (String key : mIatResults.keySet()) {
                resultBuffer.append(mIatResults .get(key));
            }

            webView.loadUrl("https://www.baidu.com/s?ie=UTF-8&wd="+resultBuffer.toString());
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    }

    class MyInitListener implements InitListener {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败 ");
            }

        }
    }

    /**
     * 语音识别
     */
    private void startSpeech() {
        //1. 创建SpeechRecognizer对象，第二个参数： 本地识别时传 InitListener
        SpeechRecognizer mIat = SpeechRecognizer.createRecognizer( this, null); //语音识别器
        //2. 设置听写参数，详见《 MSC Reference Manual》 SpeechConstant类
        mIat.setParameter(SpeechConstant. DOMAIN, "iat" );// 短信和日常用语： iat (默认)
        mIat.setParameter(SpeechConstant. LANGUAGE, "zh_cn" );// 设置中文
        mIat.setParameter(SpeechConstant. ACCENT, "mandarin" );// 设置普通话
        //3. 开始听写
        mIat.startListening( mRecoListener);
    }

    // 听写监听器
    private RecognizerListener mRecoListener = new RecognizerListener() {
        // 听写结果回调接口 (返回Json 格式结果，用户可参见附录 13.1)；
    //一般情况下会通过onResults接口多次返回结果，完整的识别内容是多次结果的累加；
    //关于解析Json的代码可参见 Demo中JsonParser 类；
    //isLast等于true 时会话结束。
        public void onResult(RecognizerResult results, boolean isLast) {
            Log.e (TAG, results.getResultString());
            System.out.println(results.getResultString()) ;
            showTip(results.getResultString()) ;
        }

        // 会话发生错误回调接口
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true)) ;
            // 获取错误码描述
            Log. e(TAG, "error.getPlainDescription(true)==" + error.getPlainDescription(true ));
        }

        // 开始录音
        public void onBeginOfSpeech() {
            showTip(" 开始录音 ");
        }

        //volume 音量值0~30， data音频数据
        public void onVolumeChanged(int volume, byte[] data) {
            showTip(" 声音改变了 ");
        }

        // 结束录音
        public void onEndOfSpeech() {
            showTip(" 结束录音 ");
        }

        // 扩展用接口
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
        }
    };

    private void showTip (String data) {
        Toast.makeText( this, data, Toast.LENGTH_SHORT).show() ;
    }

}
