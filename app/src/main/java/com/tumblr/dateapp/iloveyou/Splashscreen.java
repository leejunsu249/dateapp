package com.tumblr.dateapp.iloveyou;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splashscreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        Handler handler = new Handler(); //핸들러 객체 생성
        //3초 후 Handler 시작
        handler.postDelayed(new SHandler(), 2000);
    }

    private class SHandler implements Runnable {
        public void run() {
            //MainActivity.class 실행
            Intent intent = new Intent(
                    getApplicationContext(), MainActivity.class);
            startActivity(intent);

            //Splash 화면을 Activity스택에서 제거.
            Splashscreen.this.finish();
        }
    }


}