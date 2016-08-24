package com.marorobot.dronepad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

    }
}
