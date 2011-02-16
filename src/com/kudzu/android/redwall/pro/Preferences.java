package com.kudzu.android.redwall.pro;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		mContext = this;

		Preference versionPreference = getPreferenceScreen().findPreference(
				"pref_version");
		versionPreference.setTitle(getString(R.string.pref_version_title,
				getRedWallVersion()));
		
		versionPreference.setOnPreferenceClickListener(this);
		DBHelper db = new DBHelper(this);
		versionPreference.setSummary(getString(R.string.pref_version_summary,
				db.getDBVersion()));
		db.close();

		
		  Preference clearCachePreference =
		  getPreferenceScreen().findPreference("pref_clear_cache");
		  clearCachePreference.setOnPreferenceClickListener(this);
		 
		  Preference clearWpPreference =
		  getPreferenceScreen().findPreference("pref_clear_wp");
		  clearWpPreference.setOnPreferenceClickListener(this);
		 
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("pref_notifications")) {
			/*
			 * DBHelper db = new DBHelper(this);
			 * db.setNotifications(sharedPreferences
			 * .getBoolean("pref_notifications", true)); db.close();
			 */
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference.getKey().equals("pref_version")) {
			// Toast.makeText(this, R.string.checking,
			// Toast.LENGTH_SHORT).show();
			// new Updater(this, Su.getSuVersion(mContext)).doUpdate();
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.HELP_URL));
			startActivity(myIntent);
			return true;
		} else if (preference.getKey().equals("pref_clear_cache")) {
			
			File[] files = Constants.CACHE_STORAGE.listFiles();
			for (File f : files)
				f.delete();

			return true;
		} else if (preference.getKey().equals("pref_clear_wp")) {

			File[] files = Constants.EXTERNAL_STORAGE.listFiles();
			for (File f : files)
				f.delete();

			DBHelper db = new DBHelper(this);
			db.clearWp();
			db.close();

			return true;
		} else {
			return false;
		}
	}

	private String getRedWallVersion() {
		String versionName = "";

		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(
					"com.kudzu.android.redwall.pro",
					PackageManager.GET_META_DATA);
			versionName = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return versionName;
	}

}