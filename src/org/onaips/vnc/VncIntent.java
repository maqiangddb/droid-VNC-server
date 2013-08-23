package org.onaips.vnc;

/**
 * Created by mqddb on 13-8-23.
 */
public class VncIntent {
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
}
