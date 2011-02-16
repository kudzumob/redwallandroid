package com.kudzu.android.redwall.pro;

import java.io.File;

import android.os.Environment;

public class Constants {

	public static File EXTERNAL_STORAGE = new File(
			Environment.getExternalStorageDirectory() + "/Redwall");

	public static File CACHE_STORAGE = new File(
			Environment.getExternalStorageDirectory(),
			".RedwallThumbCache");

	public static String SETTING_KEY_RUN = "frun23";
	
	public static String HELP_URL = "http://rootskudzumob.appspot.com/help.jsp?aid=ag1yb290c2t1ZHp1bW9icgwLEgRBcHBNGPnMAww";

}
