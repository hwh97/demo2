package cn.hwwwwh.demo2.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import org.reactivestreams.Subscription;

import java.util.Observable;
import java.util.Observer;

import cn.hwwwwh.demo2.R;

public class LoginActivity extends AppCompatActivity {

    private EditText codeEt;
    private EditText pwdEt;
    private Button btnRegister;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        codeEt=(EditText)findViewById(R.id.code_et);
        pwdEt=(EditText)findViewById(R.id.pwd_et);
        btnRegister=(Button) findViewById(R.id.btnRegister);
        btnLogin=(Button) findViewById(R.id.btnLogin);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EMClient.getInstance().login(codeEt.getText().toString().trim(), pwdEt.getText().toString().trim(), new EMCallBack() {
                    @Override
                    public void onSuccess() {
                        Log.d("TalkFragment", "登陆成功");
                        startActivity(new Intent(LoginActivity.this,ImActivity.class));
//                        EMClient.getInstance().chatManager().loadAllConversations();
//                        EMClient.getInstance().groupManager().loadAllGroups();
                    }

                    @Override
                    public void onError(int i, String s) {
                        Log.d("TalkFragment", "登陆失败"+s);
                    }

                    @Override
                    public void onProgress(int i, String s) {

                    }
                });
            }
        });
    }
    private void register() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EMClient.getInstance().createAccount(codeEt.getText().toString().trim(), pwdEt.getText().toString().trim());
                            Log.d("TalkFragment", "注册成功");
                        } catch (HyphenateException e) {
                            e.printStackTrace();
                            Log.e("TalkFragment", "注册失败"+e.getMessage()); } } }).start();
    }
}
