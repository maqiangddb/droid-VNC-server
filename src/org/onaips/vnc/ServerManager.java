package org.onaips.vnc;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerManager extends IntentService {

    private static final boolean ENG = false;
    SharedPreferences _preferencesManager;
	private static PowerManager.WakeLock wakeLock = null;

	boolean serverOn = false;
	public static String SOCKET_ADDRESS = "org.onaips.vnc.gui";
	SocketListener _svnListener = null;

	private String reverseHost = null;
	private final IBinder mBinder = new MyBinder();
	private Handler _uiHandler;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
    */
    public ServerManager() {
        super("VncIntentServic");
        log("ServerManager==VncIntentServic");
    }


    public class MyBinder extends Binder {
        ServerManager getService() {
            return ServerManager.this;
        }
    }

    // We return the binder class upon a call of bindService
    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        log("================onHandleIntent===");
        handIntent(intent);
    }

    @Override
	public void onCreate() {
		super.onCreate();
        log("ServerManager===onCreate");

        _uiHandler = new Handler(Looper.getMainLooper());
        _preferencesManager = PreferenceManager.getDefaultSharedPreferences(this);

		if (_svnListener != null) {
			log("_svnListener was already active!");
		} else {
			log("_svnListener started");
            _svnListener = new SocketListener();
            _svnListener.start();
		}

	}

	//for pre-2.0 devices
	@Override
	public void onStart(Intent intent, int startId) {
        log("========onStart");
        copyWebClientFile();
        handIntent(intent);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        showTextOnScreen("Droid VNC server service killed...");
        killServer();
        if (_svnListener.isAlive()) {
            _svnListener.finishThread();
        }
    }



	@TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        log("========onStartCommand");
        //handIntent(intent);
		return super.onStartCommand(intent,flags,startId);
	}

    private void copyWebClientFile() {

        MainApplication.createBinaries(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void handIntent(Intent intent) {
        log("onHandleIntent==intent:"+intent);
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(VncServerReceiver.START_VNC_ACTION)) {
        Bundle settings = intent.getBundleExtra(VncServerReceiver.EXTRA_SETTINGS_DATA);
        if (settings != null) {
            log("have settings===");
            String password = settings.getString(VncServerReceiver.KEY_PASSWORD, "");
            String port = settings.getString(VncServerReceiver.KEY_PORT, "843");
            String rotate = settings.getString(VncServerReceiver.KEY_ROTATION, "0");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            saveSettings(preferences, password, port, rotate);
        }
        startServer();
        } else if (action.equals(VncServerReceiver.REQUST_STATE_ACTION)) {

            int state = VncServerReceiver.STATE_STOPED;
            if (isServerRunning()) {
                state = VncServerReceiver.STATE_STARTED;
            }
            checkNetworkConnect();
            sendStateIntent(state);
        } else if (action.equals(VncServerReceiver.STOP_VNC_ACTION)) {
            if (isServerRunning()) {
                killServer();
            } else {
                log("vnc already stoped");
                sendStateIntent(VncServerReceiver.STATE_STOPED);
            }
        }
    }

    private void checkNetworkConnect() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        log("checkNetworkConnect===active info:"+info);
    }

    private void saveSettings(SharedPreferences preferences, String password, String port, String rotate) {
        log("saveSettings==password:"+password+"=port:"+port+"==rotate:"+rotate);
        SharedPreferences.Editor editor = preferences.edit();
        if (checkString(password)) {
            editor.putString(VncSettings.KEY_PASSWORD, password);
        }
        if (checkString(port)) {
            editor.putString(VncSettings.KEY_PORT, port);
        }
        if (checkString(rotate)) {
            editor.putString(VncSettings.KEY_ROTATION, rotate);
        }
        editor.commit();
    }

    private boolean checkString (String msg) {

        return (msg !=null && msg != "");
    }

	public void startServer() {
		// Lets see if i need to boot daemon...
		try {
			Process sh;
			String files_dir = getFilesDir().getAbsolutePath();

			String password = _preferencesManager.getString(VncSettings.KEY_PASSWORD, "12345678");
			String password_check = "";
			if (!password.equals(""))
				password_check = "-p " + password;

            String ip_string = "";
            ip_string = "-i " + Util.getIpAddress();

			String rotation = "0";//_preferencesManager.getString(VncSettings.KEY_ROTATION, "0");
			if (!rotation.equals(""))
				rotation = "-r " + rotation;

			String scaling = _preferencesManager.getString(VncSettings.KEY_SCALE, "100");

			String scaling_string = "";
			if (!scaling.equals(""))
				scaling_string = "-s " + scaling;

			String port = _preferencesManager.getString(VncSettings.KEY_PORT, "843");
			try {
				int port1 = Integer.parseInt(port);
				port = String.valueOf(port1);
			} catch (NumberFormatException e) {
				port = "843";
			}
			String port_string = "";
			if (!port.equals(""))
				port_string = "-P " + port;

			String reverse_string = "";
			if (reverseHost != null && !reverseHost.equals(""))
				reverse_string = "-R " + reverseHost;


			String display_method = "";
			if (!_preferencesManager.getString(VncSettings.KEY_DISPLAY_MODE, "auto").equals("auto"))
				display_method = "-m " + _preferencesManager.getString(VncSettings.KEY_DISPLAY_MODE, "auto");
			
			//our exec file is disguised as a library so it will get packed to lib folder according to cpu_abi
			String droidvncserver_exec=getFilesDir().getParent()
                    + "/lib/libandroidvncserver.so";
            droidvncserver_exec = "/system/lib/libandroidvncserver.so";
			File f=new File (droidvncserver_exec);

			if (!f.exists())
			{
				String e="Error! Could not find daemon file, " + droidvncserver_exec;
				showTextOnScreen(e);
				log(e);
				return;
			}
			
			
			Process p = Runtime.getRuntime().exec("chmod 777 " + droidvncserver_exec);
            log("running chmod 777:"+p);
 
			String permission_string="chmod 777 " + droidvncserver_exec;
			String server_string= droidvncserver_exec + " " + ip_string  + " " + password_check + " " + rotation+ " " + scaling_string + " " + port_string + " "
			+ reverse_string + " " + display_method ;

            log("====exec:"+server_string);

			boolean root=_preferencesManager.getBoolean(VncSettings.KEY_AROOT,true);
            log("MainActivity has Root permission:"+MainActivity.hasRootPermission());
			root &= MainActivity.hasRootPermission();

			if (root)
			{ 
				log("Running as root...files_dir:"+files_dir);

				sh = Runtime.getRuntime().exec("/system/xbin/su",null,new File(files_dir));
                log("running su result:"+sh.toString());
				OutputStream os = sh.getOutputStream();
				writeCommand(os, permission_string);//run [chmod 777 droidvncserver_exec]
				writeCommand(os, server_string);// run []

			}
			else
			{
				log("Not running as root...");
				Runtime.getRuntime().exec(permission_string);
				Runtime.getRuntime().exec(server_string,null,new File(files_dir));
			}
			// dont show password on logcat
			log("Starting " + droidvncserver_exec + "password:" + !password_check.equals("")  + " " + rotation+ " " + scaling_string + " " + port_string + " "
					+ reverse_string + " " + display_method + " ");

		} catch (IOException e) {
			log("startServer():" + e.getMessage());
            e.printStackTrace();
		} catch (Exception e) {
			log("startServer():" + e.getMessage());
            e.printStackTrace();
		}

	}

    void killServer() {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress addr = InetAddress.getLocalHost();
            String toSend = "~KILL|";
            byte[] buffer = toSend.getBytes();

            DatagramPacket question = new DatagramPacket(buffer, buffer.length,
                    addr, 13132);
            clientSocket.send(question);
        } catch (Exception e) {

        }
    }

	void startReverseConnection(String host) {
		try {
            reverseHost = host;

			if (isServerRunning()) {
				killServer();
				Thread.sleep(2000);
			}

			startServer();
            reverseHost = null;

		} catch (InterruptedException e) {
			log(e.getMessage());
		}
	}



    private void sendStateIntent(int state) {

        Intent intent = new Intent(VncServerReceiver.VNC_STATE_CHANGE_ACTION);
        intent.putExtra(VncServerReceiver.EXTRA_STATE, state);
        intent.putExtra(VncServerReceiver.EXTRA_IP, Util.getIpAddress());
        intent.putExtra(VncServerReceiver.EXTRA_HTTP_PORT, Util.getHttpPort(this));
        log(">>>>>>send intent:" + intent);
        sendBroadcast(intent);
    }

    private void sendConnectIntent(String host, boolean connected) {
        Intent intent = new Intent(VncServerReceiver.VNC_CONNECT_CHANGE_ACTION);
        Bundle data = new Bundle();
        data.putString(VncServerReceiver.KEY_HOST, host);
        data.putBoolean(VncServerReceiver.KEY_CONNECTED, connected);
        intent.putExtra(VncServerReceiver.EXTRA_DATA, data);
        log(">>>>>>send intent:"+intent);
        sendBroadcast(intent);
    }

    public void showClientConnected(String c) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        int icon = R.drawable.icon;
        CharSequence tickerText = c + " connected to VNC server";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        Context context = getApplicationContext();
        CharSequence contentTitle = "Droid VNC Server";
        CharSequence contentText = "Client Connected from " + c;
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);

        mNotificationManager.notify(MainActivity.APP_ID, notification);

        // lets see if we should keep screen on
        if (_preferencesManager.getBoolean(VncSettings.KEY_SCREEN_OFF, false)) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "VNC");
            wakeLock.acquire();
        }
    }

    void showClientDisconnected() {
        log("showClientDisconnected===");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(MainActivity.APP_ID);

        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
    }

    public void showTextOnScreen(final String t) {
        if (!ENG) {
            return;
        }
        _uiHandler.post(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                Toast.makeText(getApplicationContext(), t, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    public void log(String s) {
        Log.v(MainActivity.VNC_LOG, s);
    }

    static void writeCommand(OutputStream os, String command) throws Exception {
        os.write((command + "\n").getBytes("ASCII"));
    }
	
	public static boolean isServerRunning() {
		try {
			byte[] receiveData = new byte[1024];
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress addr = InetAddress.getLocalHost();

			clientSocket.setSoTimeout(100);
			String toSend = "~PING|";
			byte[] buffer = toSend.getBytes();

			DatagramPacket question = new DatagramPacket(buffer, buffer.length,
					addr, 13132);
			clientSocket.send(question);

			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String receivedString = new String(receivePacket.getData());
			receivedString = receivedString.substring(0, receivePacket
					.getLength());

			return receivedString.equals("~PONG|");
		} catch (Exception e) {
			return false;
		}
	}

	class SocketListener extends Thread {
		DatagramSocket server = null;
		boolean finished = false;

		public void finishThread() {
			finished = true;
		}

		@Override
		public void run() {
			try {
                if (server == null) {
				    server = new DatagramSocket(13131);
                }
				log("Listening...");

				while (!finished) {
					DatagramPacket answer = new DatagramPacket(new byte[1024],
							1024);
					server.receive(answer);

					String resp = new String(answer.getData());
					resp = resp.substring(0, answer.getLength());

					log("RECEIVED " + resp);  

					if (resp.length() > 5
							&& resp.substring(0, 6).equals("~CLIP|")) {
						//resp = resp.substring(7, resp.length() - 1);
						//ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

						//clipboard.setText(resp.toString());
					} else if (resp.length() > 6
							&& resp.substring(0, 6).equals("~SHOW|")) {
						resp = resp.substring(6, resp.length() - 1);
						showTextOnScreen(resp);
					} else if (resp.length() > 15
							&& (resp.substring(0, 15).equals("~SERVERSTARTED|") ||
                            resp.substring(0, 15).equals("~SERVERSTOPPED|"))) {

                        if (resp.substring(0, 15).equals("~SERVERSTARTED|")) {
                            sendStateIntent(VncServerReceiver.STATE_STARTED);
                        } else if (resp.substring(0, 15).equals("~SERVERSTOPPED|")) {
                            sendStateIntent(VncServerReceiver.STATE_STOPED);
                            showClientDisconnected();
                        }

					} 
					else if (_preferencesManager.getBoolean(VncSettings.KEY_NOTIFY_CLIENT, true)) {
						if (resp.length() > 10
								&& resp.substring(0, 11).equals("~CONNECTED|")) {
							String host = resp.substring(11, resp.length() - 1);
                            sendConnectIntent(host, true);
							showClientConnected(host);
						} else if (resp.length() > 13
								&& resp.substring(0, 14).equals(
								"~DISCONNECTED|")) {
                            String host = resp.substring(14, resp.length() - 1);
                            sendConnectIntent(host, false);
							showClientDisconnected();
						}
					} else {
						log("Received: " + resp);
					}
				}
			} catch (IOException e) {
				log("ERROR em SOCKETLISTEN " + e.getMessage());
                e.printStackTrace();
			}
		}
	}

}
