package com.tumblr.dateapp.iloveyou;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tumblr.dateapp.iloveyou.activities.OnlineActivity;
import com.tumblr.dateapp.iloveyou.adapters.MainFragmentPagerAdapter;
import com.tumblr.dateapp.iloveyou.constants.Constant;
import com.tumblr.dateapp.iloveyou.constants.Database;
import com.tumblr.dateapp.iloveyou.fragments.LoginFragment;
import com.tumblr.dateapp.iloveyou.fragments.RegisterFragment;
import com.tumblr.dateapp.iloveyou.models.UserConversations;
import com.tumblr.dateapp.iloveyou.utils.DatabaseUtils;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener,
        RegisterFragment.OnFragmentInteractionListener {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference database;

    private ViewPager viewPager;
    private ProgressBar progressBar;

    private MainFragmentPagerAdapter mainFragmentPagerAdapter;
    private BackPressCloseHandler backPressCloseHandler;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        MobileAds.initialize(this, "ca-app-pub-4192493935458488~7773586429");
        backPressCloseHandler = new BackPressCloseHandler(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        AppBarLayout.LayoutParams toolParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        toolParams.setScrollFlags(0);
        toolbar.setLayoutParams(toolParams);

        mainFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager());

        viewPager = (ViewPager) findViewById(R.id.container_main);
        viewPager.setAdapter(mainFragmentPagerAdapter);

        progressBar = (ProgressBar) findViewById(R.id.main_spinner);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs_main);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        //푸시토큰서버
//        passPushTokenToServer();
    }

    @Override
    public void onStart() {
        super.onStart();

        user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(MainActivity.this, "로그인 또는 새로운 계정을 만들어주세요", Toast.LENGTH_LONG).show();
        } else {
            mountOnlineActivity();
        }
    }


    // app logic

    @Override
    public void requestLogin(String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Constant.NEARBY_CHAT, "signInWithEmail:success");
                        user = auth.getCurrentUser();

                        mountOnlineActivity();

                        Log.d(Constant.NEARBY_CHAT, user.getEmail() != null ? user.getEmail() : "EMPTY");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(Constant.NEARBY_CHAT, "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.GONE);
                });
    }

    private void initProfileImage() {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        DatabaseUtils.saveProfilePicture(bitmap, null, null);
    }

    @Override
    public void requestRegister(String username, String email, String password) {

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Constant.NEARBY_CHAT, "createUserWithEcmail:sucess");
                        user = auth.getCurrentUser();
                        Log.d(Constant.NEARBY_CHAT, user.getEmail() != null ? user.getEmail() : "EMPTY");
                        initProfileImage();
                        createUserConversationEntry();
                        mountOnlineActivity();

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(Constant.NEARBY_CHAT, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.GONE);
                });
    }


    public void mountOnlineActivity() {
        Intent intent = new Intent(this, OnlineActivity.class);
        startActivity(intent);
    }

    private void createUserConversationEntry() {

        String key = database
                .child(Database.userConversations)
                .push()
                .getKey();

        UserConversations userConversations = new UserConversations();
        userConversations.setOwnerId(DatabaseUtils.getCurrentUUID());
        userConversations.setId(key);
        userConversations.setConversations(new HashMap<>());

        DatabaseUtils.getCurrentDatabaseReference()
                .child(Database.userConversations)
                .child(user.getUid())
                .setValue(userConversations);
    }
//푸시토큰서버

//    void passPushTokenToServer(){
//
//        String uid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
//        String token = FirebaseInstanceId.getInstance().getToken();
//        Map<String,Object> map = new HashMap<>();
//        map.put("putToken",token);
//
//
//        FirebaseDatabase.getInstance().getReference().child("userProfiles").child(uid).updateChildren(map);
//
//
//    }



    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }




}