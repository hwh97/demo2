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
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.ui.EaseConversationListFragment;

import cn.hwwwwh.demo2.R;

public class ImActivity extends AppCompatActivity {

    EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im);
        username=(EditText) findViewById(R.id.username);
        findViewById(R.id.beginChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!username.getText().toString().trim().equals("")){
                    startChat(0);
                }
            }
        });
        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        findViewById(R.id.allChat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat(1);
            }
        });
        findViewById(R.id.friend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChat(2);
            }
        });
    }

    //启动
    public void startChat(int type){
        Intent intent=new Intent(ImActivity.this,ChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_USER_ID,username.getText().toString().trim());
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, EMMessage.ChatType.Chat);
        intent.putExtra("type",type);
        startActivity(intent);
    }

    public void logout(){
        EMClient.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                Log.d("TalkFragment", "退出登陆成功");
                startActivity(new Intent(ImActivity.this,LoginActivity.class));
                finish();
            }

            @Override
            public void onError(int i, String s) {
                Log.d("TalkFragment", "退出登陆失败"+s);
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });
    }

}
