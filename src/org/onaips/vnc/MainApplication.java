package org.onaips.vnc;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainApplication extends Application {

 

	@Override 
	public void onCreate() {
		super.onCreate(); 
		//if (firstRun()) 
			//createBinaries(this);
	}    
 
	public void log(String s)
	{
        if (Util.ENG) {
		    Log.v(MainActivity.VNC_LOG,s);
        }
	}

	public boolean firstRun()
	{
		int versionCode = 0;
		try {
			versionCode = getPackageManager()
			.getPackageInfo(getPackageName(), PackageManager.GET_META_DATA)
			.versionCode;
		} catch (NameNotFoundException e) {
			log("Package not found... Odd, since we're in that package..." + e.getMessage());
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int lastFirstRun = prefs.getInt("last_run", 0);

		if (lastFirstRun >= versionCode) {
			log("Not first run");
			return false; 
		}
		log("First run for version " + versionCode); 

		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("last_run", versionCode);
		editor.commit(); 
		return true;
	}   
 
	public static void createBinaries(Context context)
	{   
		String filesdir = context.getFilesDir().getAbsolutePath()+"/";
 
		//copy html related stuff
        File client = new File(filesdir+"/webclients.zip");
        if (!client.exists()) {
            Util.LOGI("webClient zip file not exists, copy....");
		    copyBinary(context, R.raw.webclients, filesdir + "/webclients.zip");
            //copy ssl related stuff
            copyBinary(context, R.raw.self, filesdir + "/self.pem");
        }
		 
		try {
			//ResLoader.unpackResources(R.raw.webclients, context.getApplicationContext(),filesdir);
            File web = new File(filesdir+"/webclients");
            if (!web.exists()) {
                Util.LOGI("webClient zip unpacking....");
                ResLoader.unpackResources(filesdir+"/webclients.zip", filesdir);
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

    /* copy raw file to file path
    *
     */
	public static void copyBinary(Context context, int id,String path)
	{
		Util.LOGI("copy -> " + path);
		try {
			InputStream ins = context.getResources().openRawResource(id);
			int size = ins.available();

			// Read the entire resource into a local byte buffer.
			byte[] buffer = new byte[size];
			ins.read(buffer);
			ins.close();

			FileOutputStream fos = new FileOutputStream(path);
			fos.write(buffer);
			fos.close();
		}
		catch (Exception e)
		{
			Util.LOGI("public void createBinary() error! : " + e.getMessage());
		}
	}  

	static void writeCommand(OutputStream os, String command) throws Exception
	{
		os.write((command + "\n").getBytes("ASCII"));
	} 
}
