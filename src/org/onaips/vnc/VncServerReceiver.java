package org.onaips.vnc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

public class VncServerReceiver extends BroadcastReceiver
{
    private static final String TAG = VncServerReceiver.class.getName();

    public static final String START_VNC_ACTION = "com.samkoon.vnc.START";
    public static final String STOP_VNC_ACTION = "com.samkoon.vnc.STOP";
    public static final String REBOOT_VNC_ACTION = "com.samkoon.vnc.REBOOT";
    public static final String EXTRA_SETTINGS_DATA = "com.samkoon.vnc.SETTINGS";
    public static final String KEY_PASSWORD = "com.samkoon.vnc.PASSWORD";//password
    public static final String KEY_PORT = "com.samkoon.vnc.PORT";//port
    public static final String KEY_ROTATION = "com.samkoon.vnc.ROTATION";//rotation
    public static final String KEY_SCALE = "com.samkoon.vnc.SCALE";//scale

    public static final String VNC_STATE_CHANGE_ACTION = "com.samkoon.vnc.STATE_CHANGE";
    public static final String EXTRA_STATE = "com.samkoon.vnc.STATE";
    public static final int STATE_STARTED = 0;
    public static final int STATE_STOPED = 1;

    public static final String VNC_CONNECT_CHANGE_ACTION = "com.samkoon.vnc.CONNECT_CHANGE";
    public static final String EXTRA_DATA = "com.samkoon.vnc.DATA";
    public static final String KEY_HOST = "com.samkoon.vnc.HOST";
    public static final String KEY_CONNECTED = "com.samkoon.vnc.CONNECTED";

    private void showUse(Context context) {
        Intent send = new Intent(START_VNC_ACTION);
        Bundle settings = new Bundle();
        settings.putString(KEY_PORT, "[port]");
        settings.putString(KEY_PASSWORD, "[password]");
        send.putExtra(EXTRA_SETTINGS_DATA, settings);
        context.sendBroadcast(send);

        Intent receive = null;
        if (receive.getAction().equals(VNC_STATE_CHANGE_ACTION)) {
            int state = receive.getIntExtra(EXTRA_STATE, STATE_STOPED);
            switch (state) {
                case STATE_STARTED:
                    break;
                case STATE_STOPED:
                    break;
            }
        }

        if (receive.getAction().equals(VNC_CONNECT_CHANGE_ACTION)) {
            Bundle data = receive.getBundleExtra(EXTRA_DATA);
            String host = data.getString(KEY_HOST);
            boolean connected = data.getBoolean(KEY_CONNECTED);
        }

    }

	@Override
	public void onReceive(Context context, Intent intent) 
	{
        Log.i(MainActivity.VNC_LOG, "<<<<<<<<<<<VncServerReceiver===onReceiver=intent:"+intent);

        String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			Intent i = new Intent(context,ServerManager.class);
			context.startService(i);
		}  else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            checkNetworkInfo(context);
        } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            Log.i(TAG, "===wifi supplicant Connection Change");
            boolean state = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
            Log.i(TAG, "===connection state:"+state);
        } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            Log.i(TAG, "====wifi state changed====");
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            switch (state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    Log.i(TAG, "WIFI_STATE_DISABLED");
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    Log.i(TAG, "WIFI_STATE_DISABLING");
                    break;
                case  WifiManager.WIFI_STATE_UNKNOWN:
                    Log.i(TAG, "WIFI_STATE_UNKNOWN");
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    Log.i(TAG, "WIFI_STATE_ENABLED");
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    Log.i(TAG, "WIFI_STATE_ENABLING");
                    break;
            }
        } else if (action.equals(START_VNC_ACTION)) {
            Bundle d = intent.getBundleExtra(EXTRA_SETTINGS_DATA);
            Intent i = new Intent(context.getApplicationContext(), ServerManager.class);
            i.putExtra(EXTRA_SETTINGS_DATA, d);
            context.startService(i);
        } else if (action.equals(STOP_VNC_ACTION)) {
            Intent i = new Intent(context.getApplicationContext(), ServerManager.class);
            context.stopService(i);
        } else if (action.equals(VNC_STATE_CHANGE_ACTION)) {

        }
	}

    private void checkNetworkInfo(Context context) {
        Log.i(TAG, "checkNetworkInfo");
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        Log.i(TAG, "=ActiveInfo:"+activeInfo);
        NetworkInfo[] infos = manager.getAllNetworkInfo();
        for (NetworkInfo i : infos) {
            Log.i(TAG, "===info:"+i+"=Available="+i.isAvailable()+"=Connected="+i.isConnected()+"=connectedORConnecting="+i.isConnectedOrConnecting());

        }
    }

}  