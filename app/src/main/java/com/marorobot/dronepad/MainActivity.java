package com.marorobot.dronepad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.FollowApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.gcs.follow.FollowState;
import com.o3dr.services.android.lib.gcs.follow.FollowType;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ssundance on 2016-08-21.
 */
public class MainActivity extends AppCompatActivity implements DronePadApp.ApiListener {

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
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    alertUser("드론과 연결이 끊겼습니다.");
                    updateConnectedButton(false);
                    break;

                case AttributeEvent.AUTOPILOT_ERROR:
                    final String errorName = intent.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID);
                    break;

                default:

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dPad = (DronePadApp) getApplication();
        this.drone = dPad.getDrone();
        updateConnectedButton(this.drone.isConnected());

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        findViewById(R.id.btnFollowMe).setOnTouchListener(onTouchListenerFollowMe);
        findViewById(R.id.btnTakeOff).setOnTouchListener(onTouchListenerTakeOff);
        findViewById(R.id.btnArmed).setOnTouchListener(onTouchListenerArm);
        findViewById(R.id.btnManualFlight).setOnTouchListener(onTouchListenerManual);
        findViewById(R.id.btnLanding).setOnTouchListener(onTouchListenerLanding);

    }

    Button.OnTouchListener onTouchListenerFollowMe = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_followme_hover);
                    break;
                case MotionEvent.ACTION_UP:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_followme);
                    break;
            }
            return false;
        }
    };

    Button.OnTouchListener onTouchListenerTakeOff = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_takeoff_hover);
                    break;
                case MotionEvent.ACTION_UP:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_takeoff);
                    break;
            }
            return false;
        }
    };

    Button.OnTouchListener onTouchListenerArm = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_armed_hover);
                    break;
                case MotionEvent.ACTION_UP:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_armed);
                    break;
            }
            return false;
        }
    };

    Button.OnTouchListener onTouchListenerManual = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_manualflight_hover);
                    break;
                case MotionEvent.ACTION_UP:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_manualflight);
                    break;
            }
            return false;
        }
    };

    Button.OnTouchListener onTouchListenerLanding = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_landdisarmed_hover);
                    break;
                case MotionEvent.ACTION_UP:
                    ((Button) view).setBackgroundResource(R.drawable.main_btn_landdisarmed);
                    break;
            }
            return false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        dPad.addApiListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        dPad.removeApiListener(this);
    }

    @Override
    public void onApiConnected() {

    }

    @Override
    public void onApiDisconnected() {

    }

    public void onBtnConnect(View view) {
        if(this.drone.isConnected()) {
            this.drone.disconnect();
        } else {
            //Bundle extraParams = new Bundle();
            //extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, 14550); // Set default port to 14550
            //extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, 57600); // Set default baud rate to 57600



            //ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_BLUETOOTH, extraParams, null);
            //String address = this.btDevice.getAddress();
            //ConnectionParameter connectionParams = ConnectionParameter.newBluetoothConnection(address);


            //this.drone.connect(connectionParams);



            //DronePadApp.connectToDrone(getApplicationContext());
            dPad.connectToDrone();

        }
    }

    public void onBtnArm(View view) {
        Button thisButton = (Button)view;
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);

        /*if (vehicleState.isFlying()) {
            // Land
            this.drone.changeVehicleMode(VehicleMode.COPTER_LAND);
        } else if (vehicleState.isArmed()) {
            // Take off
            this.drone.doGuidedTakeoff(10); // Default take off altitude is 10m
        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("먼저 드론과 연결하십시오.");
        } else if (vehicleState.isConnected() && !vehicleState.isArmed()){
            // Connected but not Armed
            //this.drone.arm(true);
            VehicleApi.getApi(this.drone).arm(true);
        }*/

        if (!vehicleState.isConnected()) {
            alertUser("먼저 드론과 연결하십시오.");
        } else if (vehicleState.isConnected() && !vehicleState.isArmed()){// Connected but not Armed
            List<Parameter> parametersList = new ArrayList<Parameter>();

            // Pre Arm Check Pass
            Parameter armingCheck  = new Parameter("ARMING_CHECK", 0, 0);
            parametersList.add(armingCheck);

            // 위에 코드 안되면
            // the radio calibration has not been performed
            // RC3_MIN and RC3_MAX must have been changed from their default values (1100 and 1900) and for channels 1 to 4,
            // the MIN must be less than 1300 and the MAX greater than 1700.
            //Parameter rc3Min  = new Parameter("RC3_MIN", 1101, 1101);
            //parametersList.add(rc3Min);
            //Parameter rc3Max  = new Parameter("RC3_MAX", 1901, 1901);
            //parametersList.add(rc3Max);

            Parameters ps = new Parameters(parametersList);
            VehicleApi.getApi(this.drone).writeParameters(ps);

            //VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_GUIDED);

            VehicleApi.getApi(this.drone).arm(true, new SimpleCommandListener() {
                @Override
                public void onSuccess() {
                    alertUser("ARM 성공.");
                }

                @Override
                public void onError(int executionError) {
                    alertUser("ARM 실패. - " + executionError);
                }

                @Override
                public void onTimeout() {
                    alertUser("ARM TIMEOUT");
                }
            });

        } else if (vehicleState.isFlying()) {
            alertUser("드론이 비행중입니다.");
        }
    }

    public void onBtnTakeOff(View view) {
        Button thisButton = (Button)view;
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);

        /*if (vehicleState.isFlying()) {
            // Land
            alertUser("드론이 비행중입니다.");
            //this.drone.changeVehicleMode(VehicleMode.COPTER_LAND);
        } else if (vehicleState.isArmed()) {
            // Take off
            this.drone.doGuidedTakeoff(10); // Default take off altitude is 10m
        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("먼저 드론과 연결하십시오.");
        } else if (vehicleState.isConnected() && !vehicleState.isArmed()){
            // Connected but not Armed
            this.drone.arm(true);
        }*/

        if (vehicleState.isFlying()) {
            // Land
            alertUser("드론이 비행중입니다.");
        } else if (vehicleState.isArmed()) {
            // Take off
            //ControlApi.getApi(this.drone).takeoff(2, null); // Default take off altitude is 2m

            ControlApi.getApi(this.drone).takeoff(1, new SimpleCommandListener() {
                @Override
                public void onSuccess() {
                    VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_AUTO);
                    alertUser("TAKE OFF 성공.");
                }

                @Override
                public void onError(int executionError) {
                    alertUser("TAKE OFF 실패. - " + executionError);
                }

                @Override
                public void onTimeout() {
                    alertUser("TAKE OFF TIMEOUT");
                }
            });


        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("먼저 드론과 연결하십시오.");
        } else if (vehicleState.isConnected() && !vehicleState.isArmed()){
            // Connected but not Armed
            alertUser("ARM 버튼을 클릭하세요.");
        }
    }

    public void onBtnLanding(View view) {
        Button thisButton = (Button)view;
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);

        if (vehicleState.isFlying()) {
            // Land
            VehicleApi.getApi(this.drone).setVehicleMode(VehicleMode.COPTER_LAND);
            VehicleApi.getApi(this.drone).arm(false, new SimpleCommandListener() {
                @Override
                public void onSuccess() {
                    alertUser("DISARM 성공.");
                }

                @Override
                public void onError(int executionError) {
                    alertUser("DISARM 실패. - " + executionError);
                }

                @Override
                public void onTimeout() {
                    alertUser("DISARM TIMEOUT");
                }
            });
        } else if (vehicleState.isArmed()) {
            VehicleApi.getApi(this.drone).arm(false, new SimpleCommandListener() {
                @Override
                public void onSuccess() {
                    alertUser("DISARM 성공.");
                }

                @Override
                public void onError(int executionError) {
                    alertUser("DISARM 실패. - " + executionError);
                }

                @Override
                public void onTimeout() {
                    alertUser("DISARM TIMEOUT");
                }
            });
        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("먼저 드론과 연결하십시오.");
        } else if (vehicleState.isConnected() && !vehicleState.isArmed()){
            // Connected but not Armed
            alertUser("ARM 버튼을 클릭하세요.");
        }
    }

    public void onBtnFollowMe(View view) {
        Button thisButton = (Button)view;

        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        FollowState followState = this.drone.getAttribute(AttributeType.FOLLOW_STATE);

        if (vehicleState.isFlying() && !followState.isEnabled()) {
            alertUser("FOLLOW ME를 시작합니다.");
            FollowApi.getApi(drone).enableFollowMe(FollowType.LEASH);
        } else if (vehicleState.isFlying() && followState.isEnabled()) {
            alertUser("FOLLOW ME를 종료합니다.");
            FollowApi.getApi(drone).disableFollowMe();
        } else {
            alertUser("FOLLOW ME를 시작할 수 없습니다.");
        }
    }

    public void onBtnManual(View view) {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        
        if (vehicleState.isFlying()) {
            Intent intent = new Intent(getApplicationContext(), ManualActivity.class);
            startActivity(intent);
        } else {
            alertUser("드론이 비행상태가 아닙니다.");
        }
    }

    public void onBtnReturn(View view) {
        finish();
    }

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // 연결버튼 업데이트
    protected void updateConnectedButton(Boolean isConnected) {
        Button btnConnect = (Button)findViewById(R.id.btnConnect);
        ImageView imageConnect = (ImageView) findViewById(R.id.imageConnect);
        if (isConnected) {
            //connectButton.setText("Disconnect");
            btnConnect.setBackgroundResource(R.drawable.main_btn_disconnect);
            imageConnect.setImageResource(R.drawable.main_cnt_on);
        } else {
            //connectButton.setText("Connect");
            btnConnect.setBackgroundResource(R.drawable.main_btn_connect);
            imageConnect.setImageResource(R.drawable.main_cnt_off);
        }
    }


}
