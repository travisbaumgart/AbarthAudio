package io.baumgart.abarthaudio;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.kcoppock.bluetoothconnector.BluetoothA2DPRequester;
import com.kcoppock.bluetoothconnector.BluetoothBroadcastReceiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;


public class AbarthAudioService extends Service implements BluetoothBroadcastReceiver.Callback, com.kcoppock.bluetoothconnector.BluetoothA2DPRequester.Callback{

    static final public String COPA_RESULT = "com.controlj.copame.backend.COPAService.REQUEST_PROCESSED";
    static final public String COPA_MESSAGE = "com.controlj.copame.backend.COPAService.COPA_MSG";

    private static final String TRIGGER_DEVICE_NAME = "Blue&Me";
    private static final String TARGET_DEVICE_NAME  = "SF-LY002";

    private BluetoothDevice mTargetDevice;

    public AbarthAudioService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        //bluetooth monitoring
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);

        //get target device
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter != null) {
            Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
            for(BluetoothDevice targetDevice : devices) {
                if(TARGET_DEVICE_NAME.equals(targetDevice.getName())) {
                    mTargetDevice = targetDevice;
                }
            }

        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        sendResult("Service Started.");
    }

    @Override
    public void onDestroy() {
        sendResult("Service Destroyed.");
    }

    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            BluetoothDevice triggerDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                sendResult("Device " + triggerDevice.getName() + " connected.");

                /////////////////////
                if(TRIGGER_DEVICE_NAME.equals(triggerDevice.getName())) {
                    sendResult("Attempting to connect to " + mTargetDevice.getName() + ".");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        sendResult(e.getMessage());
                    }
                    onBluetoothConnected();
                }
                ////////////////////

            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                sendResult("Device " + triggerDevice.getName() + " disconnected.");
            }

        }

    };

    public void sendResult(String message) {
        Intent intent = new Intent(COPA_RESULT);
        if(message != null)
            intent.putExtra(COPA_MESSAGE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void onBluetoothError() {
        sendResult("There was an error enabling the Bluetooth Adapter.");
    }

    public void onBluetoothConnected() {
        (new BluetoothA2DPRequester(this)).request(this, BluetoothAdapter.getDefaultAdapter());
    }

    public void onA2DPProxyReceived(BluetoothA2dp proxy) {
        Method connect = this.getConnectMethod();
        if(connect != null && mTargetDevice != null) {
            try {
                connect.setAccessible(true);
                connect.invoke(proxy, new Object[]{mTargetDevice});
            } catch (InvocationTargetException var5) {
                sendResult("Unable to invoke connect(BluetoothDevice) method on proxy. " + var5.toString());
            } catch (IllegalAccessException var6) {
                sendResult("Illegal Access! " + var6.toString());
            }
        }
    }

    private Method getConnectMethod() {
        try {
            return BluetoothA2dp.class.getDeclaredMethod("connect", new Class[]{BluetoothDevice.class});
        }
        catch (NoSuchMethodException var2) {
            sendResult("Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
            return null;
        }
    }

}
