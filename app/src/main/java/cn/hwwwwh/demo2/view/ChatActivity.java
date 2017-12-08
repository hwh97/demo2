package cn.hwwwwh.demo2.view;

import android.app.ActionBar;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.model.EasePreferenceManager;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.ui.EaseContactListFragment;
import com.hyphenate.easeui.ui.EaseConversationListFragment;
import com.hyphenate.exceptions.HyphenateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hwwwwh.demo2.R;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        int type=getIntent().getIntExtra("type",1);
        if(type==0){
            //这里使用EaseUi
            EaseChatFragment chatFragment=new EaseChatFragment();
            chatFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.container,chatFragment).commit();
        }else if(type==1){
            EaseConversationListFragment conversationListFragment = new EaseConversationListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container,conversationListFragment).commit();
        }else if(type==2){
            getContact();
        }

    }

    private void getContact(){
        Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<String>> e) throws Exception {
                List<String> usernames  = EMClient.getInstance().contactManager().getAllContactsFromServer();
                e.onNext(usernames);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> strings) throws Exception {
                        final EaseContactListFragment easeContactListFragment=new EaseContactListFragment();
                        //需要设置联系人列表才能启动fragment
                        easeContactListFragment.setContactsMap(getContacts(strings));
                        getSupportFragmentManager().beginTransaction().add(R.id.container,easeContactListFragment).commit();
                    }
                });
    }

    /**
     * @return
     */
    private Map<String, EaseUser> getContacts(List<String> usernames ){
        Map<String, EaseUser> contacts = new HashMap<String, EaseUser>();
        for (int i=0;i<usernames.size();i++){
            EaseUser easeUser=new EaseUser(usernames.get(i));
            contacts.put("user"+1,easeUser);
        }
        return contacts;
    }
}
