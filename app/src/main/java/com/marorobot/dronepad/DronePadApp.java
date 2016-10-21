package com.marorobot.dronepad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.app.Application;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionResult;
import com.o3dr.services.android.lib.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 어플리케이션 클래스. TOWER APP 참조
 */
public class DronePadApp extends Application implements DroneListener, TowerListener {
    private static final long DELAY_TO_DISCONNECTION = 1000L; // ms
    private static final String TAG = DronePadApp.class.getSimpleName();
    public static final String ACTION_TOGGLE_DRONE_CONNECTION = Utils.PACKAGE_NAME
            + ".ACTION_TOGGLE_DRONE_CONNECTION";
    public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";
    public static final String ACTION_DRONE_CONNECTION_FAILED = Utils.PACKAGE_NAME
            + ".ACTION_DRONE_CONNECTION_FAILED";
    public static final String EXTRA_CONNECTION_FAILED_ERROR_CODE = "extra_connection_failed_error_code";
    public static final String EXTRA_CONNECTION_FAILED_ERROR_MESSAGE = "extra_connection_failed_error_message";
    private static final long EVENTS_DISPATCHING_PERIOD = 200L; //MS
    private static final AtomicBoolean isCellularNetworkOn = new AtomicBoolean(false);
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case ACTION_TOGGLE_DRONE_CONNECTION:
                    boolean connect = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION,
                            !drone.isConnected());

                    if (connect)
                        connectToDrone();
                    else
                        disconnectFromDrone();
                    break;
            }
        }
    };

    @Override
    public void onTowerConnected() {
        drone.unregisterDroneListener(this);
        controlTower.registerDrone(drone, handler);
        drone.registerDroneListener(this);
        notifyApiConnected();
    }

    @Override
    public void onTowerDisconnected() {
        notifyApiDisconnected();
    }

    public interface ApiListener {
        void onApiConnected();
        void onApiDisconnected();
    }

    private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            controlTower.unregisterDrone(drone);
            controlTower.disconnect();
            handler.removeCallbacks(this);
        }
    };

    private final Runnable eventsDispatcher = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(this);

            //Go through the events buffer and empty it
            for(Map.Entry<String, Bundle> entry: eventsBuffer.entrySet()){
                String event = entry.getKey();
                Bundle extras = entry.getValue();

                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);
            }

            eventsBuffer.clear();

            handler.postDelayed(this, EVENTS_DISPATCHING_PERIOD);
        }
    };

    private final Map<String, Bundle> eventsBuffer = new LinkedHashMap<>(200);
    private final Handler handler = new Handler();
    private final List<ApiListener> apiListeners = new ArrayList<ApiListener>();
    private Thread.UncaughtExceptionHandler exceptionHandler;
    private ControlTower controlTower;
    private Drone drone;
    private LocalBroadcastManager lbm;

    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();

        final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                exceptionHandler.uncaughtException(thread, ex);
            }
        };

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);
        lbm = LocalBroadcastManager.getInstance(context);
        controlTower = new ControlTower(context);
        drone = new Drone(context);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TOGGLE_DRONE_CONNECTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void addApiListener(ApiListener listener) {
        if (listener == null)
            return;

        handler.removeCallbacks(disconnectionTask);
        boolean isTowerConnected = controlTower.isTowerConnected();
        if (isTowerConnected)
            listener.onApiConnected();

        if (!isTowerConnected) {
            try {
                controlTower.connect(this);
            } catch (IllegalStateException e) {
                //Ignore
            }
        }

        apiListeners.add(listener);
    }

    public void removeApiListener(ApiListener listener) {
        if (listener != null) {
            apiListeners.remove(listener);
            if (controlTower.isTowerConnected())
                listener.onApiDisconnected();
        }

        shouldWeTerminate();
    }

    private void shouldWeTerminate() {
        if (apiListeners.isEmpty() && !drone.isConnected()) {
            // Wait 30s, then disconnect the service binding.
            handler.postDelayed(disconnectionTask, DELAY_TO_DISCONNECTION);
        }
    }

    private void notifyApiConnected() {
        if (apiListeners.isEmpty())
            return;

        for (ApiListener listener : apiListeners)
            listener.onApiConnected();
    }

    private void notifyApiDisconnected() {
        if (apiListeners.isEmpty())
            return;

        for (ApiListener listener : apiListeners)
            listener.onApiDisconnected();
    }

    public void connectToDrone() {
        final ConnectionParameter connParams = retrieveConnectionParameters();
        if (connParams == null)
            return;

        boolean isDroneConnected = drone.isConnected();
        if (!connParams.equals(drone.getConnectionParameter()) && isDroneConnected) {
            drone.disconnect();
            isDroneConnected = false;
        }

        if (!isDroneConnected) {
            drone.connect(connParams);
        }
    }

    public static void connectToDrone(Context context) {
        context.sendBroadcast(new Intent(DronePadApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DronePadApp.EXTRA_ESTABLISH_CONNECTION, true));
    }

    public static void disconnectFromDrone(Context context) {
        context.sendBroadcast(new Intent(DronePadApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DronePadApp.EXTRA_ESTABLISH_CONNECTION, false));
    }

    public void disconnectFromDrone() {
        if (drone.isConnected()) {
            drone.disconnect();
        }
    }

    public Drone getDrone() {
        return this.drone;
    }

    private ConnectionParameter retrieveConnectionParameters() {
        // Get the local bluetooth adapter
        BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        List<Object> mPairedDevices = new ArrayList<Object>();
        mPairedDevices.addAll(pairedDevices);
        // 드론 한개만 페어링
        if (mPairedDevices.size() == 0) {
            return null;
        } else {
            BluetoothDevice btDevice = (BluetoothDevice) mPairedDevices.get(0);
            String address = btDevice.getAddress();
            ConnectionParameter connectionParams = ConnectionParameter.newBluetoothConnection(address);

            return connectionParams;
        }
    }

    @Override
    public void onDroneConnectionFailed(ConnectionResult result) {
        String errorMsg = result.getErrorMessage();
        Toast.makeText(getApplicationContext(), "Connection failed: " + errorMsg,
                Toast.LENGTH_SHORT).show();

        lbm.sendBroadcast(new Intent(ACTION_DRONE_CONNECTION_FAILED)
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_CODE, result.getErrorCode())
                .putExtra(EXTRA_CONNECTION_FAILED_ERROR_MESSAGE, result.getErrorMessage()));
    }

    protected void alertUser(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {

        switch (event) {
            case AttributeEvent.STATE_CONNECTED: {
                //alertUser("드론과 연결되었습니다.");
                handler.removeCallbacks(disconnectionTask);
                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);

                handler.postDelayed(eventsDispatcher, EVENTS_DISPATCHING_PERIOD);
                break;
            }

            case AttributeEvent.STATE_DISCONNECTED: {
                handler.removeCallbacks(eventsDispatcher);

                shouldWeTerminate();

                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);
                break;
            }

            case AttributeEvent.SPEED_UPDATED:{
                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);

                handler.postDelayed(eventsDispatcher, EVENTS_DISPATCHING_PERIOD);
                break;
            }

            default: {
                //Buffer the remaining events, and only fire them at 30hz
                //TODO: remove this once the buffer is placed on the 3DR Services side
                eventsBuffer.put(event, extras);
                break;
            }
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        controlTower.unregisterDrone(drone);

        if (!TextUtils.isEmpty(errorMsg))
            Log.e(TAG, errorMsg);
    }
}
