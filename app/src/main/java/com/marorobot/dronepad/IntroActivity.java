package com.marorobot.dronepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;

public class IntroActivity extends AppCompatActivity {

    private Drone drone;
    private DronePadApp dPad;

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
        filter.addAction(AttributeEvent.STATE_ARMING);
        filter.addAction(AttributeEvent.AUTOPILOT_ERROR);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.STATE_CONNECTED:

                    alertUser("드론과 연결되었습니다.");
                    updateConnectedButton(true);
                    /*if (notificationHandler != null)
                        notificationHandler.init();

                    if (NetworkUtils.isOnSoloNetwork(context)) {
                        bringUpCellularNetwork(context);
                    }*/
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    alertUser("드론과 연결이 끊겼습니다.");
                    updateConnectedButton(false);
                    /*if (notificationHandler != null) {
                        notificationHandler.terminate();
                    }*/


                    break;

                case AttributeEvent.STATE_ARMING:
                    //updateArmedButton();

                case AttributeEvent.AUTOPILOT_ERROR:
                    final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    /*if (notificationHandler != null)
                        notificationHandler.onAutopilotError(errorName);*/
                    break;

                default:
                    //updateTakeOffButton();
                    //updateArmedButton();
                    //updateLandingButton();

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
    }

    public void onBtnConnectTap(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void onBtnSetupTap(View view) {
        Intent intent = new Intent(getApplicationContext(), SetupActivity.class);
        startActivity(intent);
    }

    public void onBtnPhotoTap(View view) {
        Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
        startActivity(intent);
    }

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    // 연결버튼 업데이트
    protected void updateConnectedButton(Boolean isConnected) {
        //Button btnConnect = (Button)findViewById(R.id.btnConnect);
        ImageView imageConnect = (ImageView) findViewById(R.id.introImageConnect);
        if (isConnected) {
            //connectButton.setText("Disconnect");
            //btnConnect.setBackgroundResource(R.drawable.main_btn_disconnect);
            imageConnect.setImageResource(R.drawable.main_cnt_on);
        } else {
            //connectButton.setText("Connect");
            //btnConnect.setBackgroundResource(R.drawable.main_btn_connect);
            imageConnect.setImageResource(R.drawable.main_cnt_off);
        }
    }
}
