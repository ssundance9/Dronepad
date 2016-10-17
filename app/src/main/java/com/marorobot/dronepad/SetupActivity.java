package com.marorobot.dronepad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by ssundance on 2016-10-05.
 */
public class SetupActivity extends AppCompatActivity implements DronePadApp.ApiListener {

    private Drone drone;
    private DronePadApp dPad;

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(AttributeEvent.STATE_CONNECTED);
        filter.addAction(AttributeEvent.STATE_DISCONNECTED);
        filter.addAction(AttributeEvent.STATE_UPDATED);
        filter.addAction(AttributeEvent.STATE_ARMING);
        filter.addAction(AttributeEvent.STATE_VEHICLE_MODE);
        filter.addAction(AttributeEvent.TYPE_UPDATED);
        filter.addAction(AttributeEvent.SPEED_UPDATED);
        filter.addAction(AttributeEvent.HOME_UPDATED);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case AttributeEvent.STATE_CONNECTED:
                    alertUser("드론과 연결되었습니다.");
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    alertUser("드론과 연결이 끊겼습니다.");
                    break;

                case AttributeEvent.STATE_UPDATED:
                    /*State vehicleState = this.drone.getAttribute(AttributeType.STATE);

                    if (vehicleState.isArmed()) {
                        alertUser("드론이 ARMING.");
                    } else if (vehicleState.isFlying()) {
                        alertUser("드론이 FLYING.");
                    } //else if (vehicleState.isFlying()) {
                    //alertUser("드론이 FLYING.");
                    //}*/

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
                    updateAltitude();
                    updateSpeed();
                    updateLatLong();
                    updateTime();
                    break;

                case AttributeEvent.ALTITUDE_UPDATED:
                    updateAltitude();
                    updateSpeed();
                    updateLatLong();
                    updateTime();

                case AttributeEvent.GPS_POSITION:
                    updateAltitude();
                    updateSpeed();
                    updateLatLong();
                    updateTime();
                    break;

                case AttributeEvent.HOME_UPDATED:
                    //updateDistanceFromHome();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        dPad = (DronePadApp) getApplication();
        this.drone = dPad.getDrone();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);

        TextView dateTextView = (TextView)findViewById(R.id.dateTextView);
        dateTextView.setText(new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
    }

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

    public void onBtnReturn(View view) {
        finish();
    }

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    protected void updateAltitude() {
        TextView altitudeTextView = (TextView)findViewById(R.id.altitudeValueTextView);
        Altitude altitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        altitudeTextView.setText(String.format("%3.1f", altitude.getAltitude()) + "m");
    }

    /*protected void updateAttitude() {
        TextView altitudeTextView = (TextView)findViewById(R.id.altitudeValueTextView);
        Attitude attitude = this.drone.getAttribute(AttributeType.ATTITUDE);
        altitudeTextView.setText(String.format("%3.1f", attitude.getAltitude()) + "m");
    }*/

    protected void updateSpeed() {
        TextView speedTextView = (TextView)findViewById(R.id.speedValueTextView);
        Speed speed = this.drone.getAttribute(AttributeType.SPEED);
        speedTextView.setText(String.format("%3.1f", speed.getGroundSpeed()) + "m/s");
    }

    protected void updateLatLong() {
        TextView latitudeValueTextView = (TextView)findViewById(R.id.latitudeValueTextView);
        TextView longitudeValueTextView = (TextView)findViewById(R.id.longitudeValueTextView);
        Gps droneGps = this.drone.getAttribute(AttributeType.GPS);
        LatLong position = droneGps.getPosition();
        if (position != null) {
            latitudeValueTextView.setText(String.format("%3.1f", position.getLatitude()) + "");
            longitudeValueTextView.setText(String.format("%3.1f", position.getLongitude()) + "");
        } else {
            latitudeValueTextView.setText("n/a");
            longitudeValueTextView.setText("n/a");
        }
    }

    protected void updateTime() {
        TextView timeTextView = (TextView)findViewById(R.id.timeTextView);
        timeTextView.setText(new SimpleDateFormat("a hh:mm:ss").format(new Date()));
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

}
