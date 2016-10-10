package com.marorobot.dronepad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ssundance on 2016-10-09.
 */
public class BaseActivity extends AppCompatActivity implements DroneListener, TowerListener {

    protected ControlTower controlTower;
    protected Drone drone;
    private int droneType = Type.TYPE_UNKNOWN;
    private final Handler handler = new Handler();
    /**
     * Bluetooth adapter.
     */
    protected BluetoothAdapter mBtAdapter;
    /**
     * Bluetooth device.
     */
    protected BluetoothDevice btDevice;



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

    protected Drone getDrone() {
        return this.drone;
    }
}
