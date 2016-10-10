package com.marorobot.dronepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;

public class ManualActivity extends AppCompatActivity {

    private Drone drone;
    private DronePadApp dPad;

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
        filter.addAction(AttributeEvent.AUTOPILOT_ERROR);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.STATE_CONNECTED:

                    //alertUser("드론과 연결되었습니다.");
                    //updateConnectedButton(true);
                    /*if (notificationHandler != null)
                        notificationHandler.init();

                    if (NetworkUtils.isOnSoloNetwork(context)) {
                        bringUpCellularNetwork(context);
                    }*/
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    //alertUser("드론과 연결이 끊겼습니다.");
                    //updateConnectedButton(false);
                    /*if (notificationHandler != null) {
                        notificationHandler.terminate();
                    }*/


                    break;

                case AttributeEvent.AUTOPILOT_ERROR:
                    final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    /*if (notificationHandler != null)
                        notificationHandler.onAutopilotError(errorName);*/
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        dPad = (DronePadApp) getApplication();
        this.drone = dPad.getDrone();
        //updateConnectedButton(this.drone.isConnected());

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
    }
}
