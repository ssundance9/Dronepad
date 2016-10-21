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

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

/**
 * Manual Flight 화면 Activity
 */
public class ManualActivity extends AppCompatActivity {
    // 드론
    private Drone drone;
    // 어플리케이션
    private DronePadApp dPad;
    // 이벤트 필터
    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
        filter.addAction(AttributeEvent.AUTOPILOT_ERROR);
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

                //case AttributeEvent.AUTOPILOT_ERROR:
                    //final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    //break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        dPad = (DronePadApp) getApplication();
        this.drone = dPad.getDrone();
        updateConnectedButton(this.drone.isConnected());

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        findViewById(R.id.btnForward).setOnTouchListener(onTouchListenerF);
        findViewById(R.id.btnBackward).setOnTouchListener(onTouchListenerB);
        findViewById(R.id.btnTurnR).setOnTouchListener(onTouchListenerR);
        findViewById(R.id.btnTurnL).setOnTouchListener(onTouchListenerL);
    }

    // 전진 리스너
    Button.OnTouchListener onTouchListenerF = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    goForward((Button) view);
                    break;
                case MotionEvent.ACTION_UP:
                    stopForward((Button) view);
                    break;
            }
            return false;
        }
    };

    // 후진 리스너
    Button.OnTouchListener onTouchListenerB = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    goBackward((Button) view);
                    break;
                case MotionEvent.ACTION_UP:
                    stopBackward((Button) view);
                    break;
            }
            return false;
        }
    };

    // 우회전 리스너
    Button.OnTouchListener onTouchListenerR = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    turnRight((Button) view);
                    break;
                case MotionEvent.ACTION_UP:
                    stopTurnRight((Button) view);
                    break;
            }
            return false;
        }
    };

    // 좌회전 리스너
    Button.OnTouchListener onTouchListenerL = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    turnLeft((Button) view);
                    break;
                case MotionEvent.ACTION_UP:
                    stopTurnLeft((Button) view);
                    break;
            }
            return false;
        }
    };

    // 전진 시작
    public void goForward(Button button) {
        ControlApi.getApi(this.drone).manualControl(1, 0, 0, new AbstractCommandListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int executionError) {

            }

            @Override
            public void onTimeout() {

            }
        });

        button.setBackgroundResource(R.drawable.manual_btn_on_top);
    }

    // 전진 중지
    public void stopForward(Button button) {
        stop();
        button.setBackgroundResource(R.drawable.manual_btn_off_top);
    }

    // 후진 시작
    public void goBackward(Button button) {
        ControlApi.getApi(this.drone).manualControl(-1, 0, 0, new AbstractCommandListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int executionError) {

            }

            @Override
            public void onTimeout() {

            }
        });

        button.setBackgroundResource(R.drawable.manual_btn_on_bottom);
    }

    // 후진 중지
    public void stopBackward(Button button) {
        stop();
        button.setBackgroundResource(R.drawable.manual_btn_off_bottom);
    }

    // 현위치에 대기
    public void stop() {
        ControlApi.getApi(this.drone).pauseAtCurrentLocation(new AbstractCommandListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int executionError) {

            }

            @Override
            public void onTimeout() {

            }
        });
    }

    // 우회전 시작 - 지표면에 대해 시계방향으로 회전
    public void turnRight(Button button) {
        ControlApi.getApi(this.drone).turnTo(10, 1, true, new AbstractCommandListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int executionError) {

            }

            @Override
            public void onTimeout() {

            }
        });

        button.setBackgroundResource(R.drawable.manual_btn_on_right);
    }

    // 우회전 중지
    public void stopTurnRight(Button button) {
        stop();
        button.setBackgroundResource(R.drawable.manual_btn_off_right);
    }

    // 좌회전 시작 - 지표면에 대해 반시계방향으로 회전
    public void turnLeft(Button button) {
        ControlApi.getApi(this.drone).turnTo(10, -1, true, new AbstractCommandListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int executionError) {

            }

            @Override
            public void onTimeout() {

            }
        });

        button.setBackgroundResource(R.drawable.manual_btn_on_left);
    }

    // 좌회전 중지
    public void stopTurnLeft(Button button) {
        stop();
        button.setBackgroundResource(R.drawable.manual_btn_off_left);
    }

    // 뒤로가기
    public void onBtnReturn(View view) {
        finish();
    }

    // 토스트 팝업
    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // 연결버튼 업데이트
    protected void updateConnectedButton(Boolean isConnected) {
        //Button btnConnect = (Button)findViewById(R.id.btnConnect);
        ImageView imageConnect = (ImageView) findViewById(R.id.imageConnect2);
        if (isConnected) {
            imageConnect.setImageResource(R.drawable.main_cnt_on);
        } else {
            imageConnect.setImageResource(R.drawable.main_cnt_off);
        }
    }
}
