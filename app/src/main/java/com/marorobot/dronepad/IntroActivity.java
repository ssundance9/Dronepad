package com.marorobot.dronepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;

/**
 * 초기화면 Activity
 */
public class IntroActivity extends AppCompatActivity {
    // 드론
    private Drone drone;
    // 어플리케이션
    private DronePadApp dPad;
    // 이벤트 필터
    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
        filter.addAction(AttributeEvent.STATE_ARMING);
        //filter.addAction(AttributeEvent.AUTOPILOT_ERROR);
    }

    // 이벤트 리시버
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.STATE_CONNECTED:
                    alertUser("드론과 연결되었습니다.");
                    updateConnectedButton(true);
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    alertUser("드론과 연결이 끊겼습니다.");
                    updateConnectedButton(false);
                    break;

                case AttributeEvent.STATE_ARMING:
                    break;

                //case AttributeEvent.AUTOPILOT_ERROR:
                    //final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    //break;

                default:

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        dPad = (DronePadApp) getApplication();
        this.drone = dPad.getDrone();
        updateConnectedButton(this.drone.isConnected());

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        findViewById(R.id.btnPhoto).setOnTouchListener(onTouchListener);
        findViewById(R.id.btnConnectIntro).setOnTouchListener(onTouchListener);
        findViewById(R.id.btnSetup).setOnTouchListener(onTouchListener);
    }

    // 터치 이벤트 리스터
    Button.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Button button = (Button) view;
            int btnId = button.getId();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    switch (btnId) {
                        case R.id.btnPhoto:
                            button.setBackgroundResource(R.drawable.intro_btn_photo_hover);
                            break;
                        case R.id.btnConnectIntro:
                            button.setBackgroundResource(R.drawable.intro_btn_connect_hover);
                            break;
                        case R.id.btnSetup:
                            button.setBackgroundResource(R.drawable.intro_btn_setup_hover);
                            break;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    switch (btnId) {
                        case R.id.btnPhoto:
                            button.setBackgroundResource(R.drawable.intro_btn_photo);
                            break;
                        case R.id.btnConnectIntro:
                            button.setBackgroundResource(R.drawable.intro_btn_connect);
                            break;
                        case R.id.btnSetup:
                            button.setBackgroundResource(R.drawable.intro_btn_setup);
                            break;
                    }
                    break;
            }
            return false;
        }
    };

    // 컨넥트 버튼 이벤트
    public void onBtnConnectTap(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    // 셋업 버튼 이벤트
    public void onBtnSetupTap(View view) {
        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
        startActivity(intent);
    }

    // 포토 버튼 이벤트
    public void onBtnPhotoTap(View view) {
        Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
        startActivity(intent);
    }

    // 토스트 팝업
    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // 연결버튼 업데이트
    protected void updateConnectedButton(Boolean isConnected) {
        ImageView imageConnect = (ImageView) findViewById(R.id.introImageConnect);
        if (isConnected) {
            imageConnect.setImageResource(R.drawable.main_cnt_on);
        } else {
            imageConnect.setImageResource(R.drawable.main_cnt_off);
        }
    }
}
