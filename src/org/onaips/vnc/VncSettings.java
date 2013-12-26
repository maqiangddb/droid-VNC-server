package org.onaips.vnc;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class VncSettings extends PreferenceActivity {
    private static final String KEY_WORK_NOW = "work_now";
    public final static String KEY_PASSWORD = "password";
    public final static String KEY_PORT = "port";
    public final static String KEY_ROTATION = "rotation";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_DISPLAY_MODE = "displaymode";
    public static final String KEY_NOTIFY_CLIENT = "notifyclient";
    public static final String KEY_SCREEN_OFF = "screenturnoff";
    public static final String KEY_AROOT = "asroot";
    public static final String KEY_VNC_STATE = "vnc_state";

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    addPreferencesFromResource(R.xml.preferences);
        Preference workNow = this.findPreference(KEY_WORK_NOW);
        workNow.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //Log.i("MQ", "work now preference clicked");
                VncSettings.this.finish();
                return true;
            }
        });
	}

    public static void show(Context context) {
        Intent i = new Intent(context, VncSettings.class);
        context.startActivity(i);
    }
}
