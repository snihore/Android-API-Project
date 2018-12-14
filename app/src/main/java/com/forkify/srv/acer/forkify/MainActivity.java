package com.forkify.srv.acer.forkify;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler();
        Runnable run = new Runnable() {
            int next = 0;
            @Override
            public void run() {

                if(next == 0){
                    next = 1;
                }else if(next == 1){
                    next++;
                    Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                    startActivity(intent);
                    finish();
                }
                handler.postDelayed(this,1500);

            }
        };
        handler.post(run);
    }
}
