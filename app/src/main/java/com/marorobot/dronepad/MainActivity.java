package com.marorobot.dronepad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Parameter;
import com.o3dr.services.android.lib.drone.property.Parameters;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;
import com.o3dr.services.android.lib.model.SimpleCommandListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ssundance on 2016-08-21.
 */
public class MainActivity extends AppCompatActivity implements DroneListener, TowerListener {

    private ControlTower controlTower;
    private Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private final Handler handler = new Handler();
    /**
     * Bluetooth adapter.
     */
    private BluetoothAdapter mBtAdapter;
    /**
     * Bluetooth device.
     */
    private BluetoothDevice btDevice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the service manager
        this.controlTower = new ControlTower(getApplicationContext());
        this.drone = new Drone(getApplicationContext());

        /*this.modeSelector = (Spinner)findViewById(R.id.modeSelect);
        this.modeSelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFlightModeSelected(view);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });*/

        // Get the local bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        List<Object> mPairedDevices = new ArrayList<Object>();
        mPairedDevices.addAll(pairedDevices);
        btDevice = (BluetoothDevice) mPairedDevices.get(0);



    }

    @Override
    public void onStart() {
        super.onStart();
        this.controlTower.connect(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.drone.isConnected()) {
            this.drone.disconnect();
            updateConnectedButton(false);
        }
        this.controlTower.unregisterDrone(this.drone);
        this.controlTower.disconnect();
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED:
                alertUser("드론과 연결되었습니다.");
                updateConnectedButton(this.drone.isConnected());
                break;

            case AttributeEvent.STATE_DISCONNECTED:
                alertUser("드론과 연결이 끊겼습니다.");
                updateConnectedButton(this.drone.isConnected());
                break;

            case AttributeEvent.STATE_UPDATED:
                State vehicleState = this.drone.getAttribute(AttributeType.STATE);

                if (vehicleState.isArmed()) {
                    alertUser("드론이 ARMING.");
                } else if (vehicleState.isFlying()) {
                    alertUser("드론이 FLYING.");
                } //else if (vehicleState.isFlying()) {
                    //alertUser("드론이 FLYING.");
                //}

            case AttributeEvent.STATE_ARMING:
                //alertUser("STATE_ARMING");
                //updateArmButton();
                break;

            case AttributeEvent.STATE_VEHICLE_MODE:
                //updateVehicleMode();
                //alertUser("STATE_VEHICLE_MODE");
                break;

            case AttributeEvent.TYPE_UPDATED:
                //alertUser("TYPE_UPDATED");
                /*Type newDroneType = this.drone.getAttribute(AttributeType.TYPE);
                if (newDroneType.getDroneType() != this.droneType) {
                    this.droneType = newDroneType.getDroneType();
                    updateVehicleModesForType(this.droneType);
                }*/
                break;

            case AttributeEvent.SPEED_UPDATED:
                //updateAltitude();
                //updateSpeed();
                break;

            case AttributeEvent.HOME_UPDATED:
                //updateDistanceFromHome();
                break;

            default:
                break;
        }
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {

    }

    // 3DR Services Listener
    @Override
    public void onTowerConnected() {
        this.controlTower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this);
    }

    @Override
    public void onTowerDisconnected() {

    }

    public void onBtnConnect(View view) {
        if(this.drone.isConnected()) {
            this.drone.disconnect();
        } else {
            //Bundle extraParams = new Bundle();
            //extraParams.putInt(ConnectionType.EXTRA_UDP_SERVER_PORT, 14550); // Set default port to 14550
            //extraParams.putInt(ConnectionType.EXTRA_USB_BAUD_RATE, 57600); // Set default baud rate to 57600



            //ConnectionParameter connectionParams = new ConnectionParameter(ConnectionType.TYPE_BLUETOOTH, extraParams, null);
            String address = btDevice.getAddress();
            ConnectionParameter connectionParams = ConnectionParameter.newBluetoothConnection(address);


            this.drone.connect(connectionParams);

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

            ControlApi.getApi(drone).takeoff(2, new SimpleCommandListener() {
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
            //this.drone.changeVehicleMode(VehicleMode.COPTER_LAND);
            VehicleApi.getApi(drone).setVehicleMode(VehicleMode.COPTER_LAND);
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
            alertUser("TakeOff 버튼을 클릭하세요.");
        } else if (!vehicleState.isConnected()) {
            // Connect
            alertUser("먼저 드론과 연결하십시오.");
        } else if (vehicleState.isConnected() && !vehicleState.isArmed()){
            // Connected but not Armed
            alertUser("ARM 버튼을 클릭하세요.");
        }
    }

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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

    public void onFlightModeSelected(View view) {
        //VehicleMode vehicleMode = (VehicleMode) this.modeSelector.getSelectedItem();
        //this.drone.changeVehicleMode(vehicleMode);
    }

    protected void updateVehicleModesForType(int droneType) {
        List<VehicleMode> vehicleModes =  VehicleMode.getVehicleModePerDroneType(droneType);
        ArrayAdapter<VehicleMode> vehicleModeArrayAdapter = new ArrayAdapter<VehicleMode>(this, android.R.layout.simple_spinner_item, vehicleModes);
        vehicleModeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //this.modeSelector.setAdapter(vehicleModeArrayAdapter);
    }

    protected void updateVehicleMode() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        VehicleMode vehicleMode = vehicleState.getVehicleMode();
        //ArrayAdapter arrayAdapter = (ArrayAdapter)this.modeSelector.getAdapter();
        //this.modeSelector.setSelection(arrayAdapter.getPosition(vehicleMode));
    }

    protected void updateAltitude() {
        TextView altitudeTextView = (TextView)findViewById(R.id.altitudeValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        altitudeTextView.setText(String.format("%3.1f", droneAltitude.getAltitude()) + "m");
    }

    protected void updateSpeed() {
        TextView speedTextView = (TextView)findViewById(R.id.speedValueTextView);
        Speed droneSpeed = this.drone.getAttribute(AttributeType.SPEED);
        speedTextView.setText(String.format("%3.1f", droneSpeed.getGroundSpeed()) + "m/s");
    }

    protected void updateDistanceFromHome() {
        TextView distanceTextView = (TextView)findViewById(R.id.distanceValueTextView);
        Altitude droneAltitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        double vehicleAltitude = droneAltitude.getAltitude();
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLong vehiclePosition = droneGps.getPosition();

        double distanceFromHome =  0;

        if (droneGps.isValid()) {
            LatLongAlt vehicle3DPosition = new LatLongAlt(vehiclePosition.getLatitude(), vehiclePosition.getLongitude(), vehicleAltitude);
            Home droneHome = this.drone.getAttribute(AttributeType.HOME);
            distanceFromHome = distanceBetweenPoints(droneHome.getCoordinate(), vehicle3DPosition);
        } else {
            distanceFromHome = 0;
        }

        distanceTextView.setText(String.format("%3.1f", distanceFromHome) + "m");
    }

    protected double distanceBetweenPoints(LatLongAlt pointA, LatLongAlt pointB) {
        if (pointA == null || pointB == null) {
            return 0;
        }
        double dx = pointA.getLatitude() - pointB.getLatitude();
        double dy  = pointA.getLongitude() - pointB.getLongitude();
        double dz = pointA.getAltitude() - pointB.getAltitude();
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /* protected void updateArmButton() {
        State vehicleState = this.drone.getAttribute(AttributeType.STATE);
        Button armButton = (Button)findViewById(R.id.btnArmTakeOff);

        if (!this.drone.isConnected()) {
            armButton.setVisibility(View.INVISIBLE);
        } else {
            armButton.setVisibility(View.VISIBLE);
        }

        if (vehicleState.isFlying()) {
            // Land
            armButton.setText("LAND");
        } else if (vehicleState.isArmed()) {
            // Take off
            armButton.setText("TAKE OFF");
        } else if (vehicleState.isConnected()){
            // Connected but not Armed
            armButton.setText("ARM");
        }
    } */



    public void onBtnNextTap(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
