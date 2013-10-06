package org.onaips.vnc;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by mqddb on 13-8-20.
 */
public class Util {
    public static final String VNC_TAG = "VNCserver";

    public static void LOGI(String msg) {
        Log.i(VNC_TAG, msg);
    }


    public static String getIpAddress() {

        try {
            String ipv4;
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                LOGI("===="+intf.toString());
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress()))
                            return ipv4;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String getHttpPort(Context context) {
        String port= PreferenceManager.getDefaultSharedPreferences(context).getString("port", "843");
        String httpport;
        try
        {
            int port1=Integer.parseInt(port);
            port=String.valueOf(port1);
            httpport=String.valueOf(port1-100);
        }
        catch(NumberFormatException e)
        {
            port="843";
            httpport="743";
        }
        return httpport;
    }

}
