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
    private static final String TAG = MainActivity.VNC_LOG;

    public static final String START_VNC_ACTION = "com.samkoon.vnc.START";
    public static final String STOP_VNC_ACTION = "com.samkoon.vnc.STOP";
    public static final String REBOOT_VNC_ACTION = "com.samkoon.vnc.REBOOT";
    public static final String EXTRA_SETTINGS_DATA = "com.samkoon.vnc.SETTINGS";
    public static final String KEY_PASSWORD = "com.samkoon.vnc.PASSWORD";//password
    public static final String KEY_PORT = "com.samkoon.vnc.PORT";//port
    public static final String KEY_ROTATION = "com.samkoon.vnc.ROTATION";//rotation
    public static final String KEY_SCALE = "com.samkoon.vnc.SCALE";//scale


    public static final String REQUST_STATE_ACTION = "com.samkoon.vnc.REQUEST_STATE";


    public static final String VNC_STATE_CHANGE_ACTION = "com.samkoon.vnc.STATE_CHANGE";
    public static final String EXTRA_STATE = "com.samkoon.vnc.STATE";
    public static final String EXTRA_IP = "com.samkoon.vnc.IP";
    public static final String EXTRA_HTTP_PORT = "com.samkoon.vnc.HTTP_PORT";
    public static final int STATE_STARTED = 0;
    public static final int STATE_STOPED = 1;

    public static final String VNC_CONNECT_CHANGE_ACTION = "com.samkoon.vnc.CONNECT_CHANGE";
    public static final String EXTRA_DATA = "com.samkoon.vnc.DATA";
    public static final String KEY_HOST = "com.samkoon.vnc.HOST";
    public static final String KEY_CONNECTED = "com.samkoon.vnc.CONNECTED";

	@Override
	public void onReceive(Context context, Intent intent) 
	{
        Log.i(MainActivity.VNC_LOG, "<<<<<<<<<<<VncServerReceiver===onReceiver=intent:"+intent);

        String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			//Intent i = new Intent(context,ServerManager.class);
			//context.startService(i);
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
            i.setAction(START_VNC_ACTION);
            i.putExtra(EXTRA_SETTINGS_DATA, d);
            context.startService(i);
        } else if (action.equals(STOP_VNC_ACTION)) {
            Intent i = new Intent(context.getApplicationContext(), ServerManager.class);
            i.setAction(STOP_VNC_ACTION);
            context.startService(i);
        } else if (action.equals(VNC_STATE_CHANGE_ACTION)) {
            int state = intent.getIntExtra(EXTRA_STATE, STATE_STOPED);
            String ip = intent.getStringExtra(EXTRA_IP);
            String httpPort = intent.getStringExtra(EXTRA_HTTP_PORT);
            Log.i(TAG, "ip:"+ip+"==http port :"+httpPort);
            switch (state) {
                case STATE_STOPED:
                    Log.i(TAG, "state stoped");
                    break;
                case STATE_STARTED:
                    Log.i(TAG, "state started");
                    break;
            }
        } else if (action.equals(REQUST_STATE_ACTION)) {
            Intent i = new Intent(context.getApplicationContext(), ServerManager.class);
            i.setAction(REQUST_STATE_ACTION);
            checkNetworkInfo(context);
            context.startService(i);
        }
	}

    private boolean checkNetworkInfo(Context context) {
        Log.i(TAG, "checkNetworkInfo");
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] infos = manager.getAllNetworkInfo();
        for (NetworkInfo i : infos) {
            Log.i(TAG, "===info:"+i+"=Available="+i.isAvailable()+"=Connected="+i.isConnected()+"=connectedORConnecting="+i.isConnectedOrConnecting());
        }

        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        Log.i(TAG, "=ActiveInfo:"+activeInfo);
        Log.i(TAG, "=ip:"+Util.getIpAddress());
        if(activeInfo != null) {
            Intent i = new Intent(MainActivity.UPDATE_STATE_ACTION);
            Log.i(TAG, ">>>>>>>>>>send broadcast:"+i);
            context.sendBroadcast(i);
            return true;
        } else {
            return false;
        }

    }

}  