package com.kudzu.android.redwall.pro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;

import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.Spinner;

import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	protected static final int MENU_COMMENT = Menu.FIRST + 1;
	protected static final int MENU_VIEW = Menu.FIRST + 2;
	protected static final int MENU_SET = Menu.FIRST + 3;
	protected static final int MENU_DOWNLOAD = Menu.FIRST + 4;

	protected static final int MENU_REFRESH = Menu.FIRST + 10;
	protected static final int MENU_QUIT = Menu.FIRST + 11;
	protected static final int MENU_ABOUT = Menu.FIRST + 12;
	protected static final int MENU_SEARCH = Menu.FIRST + 13;
	protected static final int MENU_PREF = Menu.FIRST + 14;

	final int ACTION_DOWNLOAD_SET = 1;
	final int ACTION_DOWNLOAD = 2;
	final int ACTION_COMMENT = 3;
	final int ACTION_OPEN = 4;

	final int NOTIFY_DATASET_CHANGED = 1;

	ProgressDialog dialog;

	DBHelper mDB;

	WallpaperAdapter adapt;
	ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();

	ListView list;
	ImageButton cmdActionBarRefresh;
	Spinner streamSpinner;

	int stream_ui_curpos = 0, stream_ui_lastpos = 0;
	boolean dont_refresh = false, dont_promt_search = false;

	// Set Wallpaper stuff, version # > 5 we use WallpaperManage
	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	WallpaperManager wallpaperManager;// = WallpaperManager.getInstance(this);

	// Prefrences
	SharedPreferences mPrefs;

	int pref_count = 50;// defaults are set in update_prefs()
	boolean pref_frist_run = true;
	boolean pref_use_compact = true;
	String pref_tap_action = "set";
	String pref_active_reddits = "redwall";

	String currentFeed = "";// this get sets at run via refresh()

	OnCreateContextMenuListener cmListener = new OnCreateContextMenuListener() {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			// TODO Auto-generated method stub
			menu.add(0, MENU_DOWNLOAD, 0, "Download wallpaper");
			menu.add(0, MENU_SET, 0, "Set as wallpaper");
			menu.add(0, MENU_VIEW, 0, "View in browser");
			menu.add(0, MENU_COMMENT, 0, "Comments / Vote / Report");
		}

	};

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case MENU_SET:
			setWallpaper(menuInfo.position);
			// open(menuInfo.position);
			return true;
		case MENU_DOWNLOAD:
			downloadWallpaper(menuInfo.position);
			// open(menuInfo.position);
			return true;
		case MENU_VIEW:
			open_image(menuInfo.position);
			return true;

		case MENU_COMMENT:
			open_comment(menuInfo.position);
			return true;

		default:
			return super.onContextItemSelected(item);
		}
	}

	private void open_web(String url) {

		Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(myIntent);
	}

	private void open_image(int id) {
		open_web(wallpapers.get(id).getImg());
	}

	private void open_comment(int id) {
		String url = "http://www.reddit.com" + wallpapers.get(id).getSrc();
		if (pref_use_compact)
			url += ".compact";
		open_web(url);
	}

	private void open_about() {
		open_web(Constants.HELP_URL);
	}

	public void downloadWallpaper(int pos) {
		if (wallpapers.get(pos).getLocalURI() == null) {
			String url = wallpapers.get(pos).getImg();
			try {
				Bitmap bitmap = BitmapFactory
						.decodeStream((InputStream) new URL(url).getContent());

				OutputStream fOut = null;
				File file = new File(Constants.EXTERNAL_STORAGE, wallpapers
						.get(pos)

						.getName() + ".jpg");

				fOut = new FileOutputStream(file);

				bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
				fOut.flush();
				fOut.close();

				// MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
				wallpapers.get(pos).setLocalURI(file.getAbsolutePath());

				String title = wallpapers.get(pos).getTitle();
				String local_uri = wallpapers.get(pos).getLocalURI();
				String perma_link = wallpapers.get(pos).getSrc();
				String thumb = wallpapers.get(pos).getThumb();

				mDB.addWp(title, local_uri, perma_link, thumb);

				Toast.makeText(Main.this, wallpapers.get(pos).getLocalURI(),
						Toast.LENGTH_LONG).show();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Toast.makeText(Main.this, "Already downloaded", Toast.LENGTH_LONG)
					.show();
		}
	}

	public void setWallpaper(int pos) {

		String uri = wallpapers.get(pos).getLocalURI();
		try {
			if (uri != null) {
				Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(
						uri));
				if (currentapiVersion >= 5) {
					wallpaperManager.setBitmap(bitmap);
				} else {
					getApplicationContext().setWallpaper(bitmap);

				}

				Toast.makeText(Main.this, "Wallpaper Updated",
						Toast.LENGTH_LONG).show();
			} else {
				downloadWallpaper(pos);
				setWallpaper(pos);
			}

		} catch (Exception e) {
			Toast.makeText(this, "Error Updaing Wallpaper", Toast.LENGTH_SHORT)
					.show();
		}
	}

	int gpos = 0;
	OnItemClickListener clickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
			gpos = pos;
			/*
			 * AlertDialog.Builder builder; AlertDialog alertDialog;
			 * 
			 * LayoutInflater inflater = (LayoutInflater) Main.this
			 * .getSystemService(LAYOUT_INFLATER_SERVICE); View layout =
			 * inflater.inflate(R.layout.info_dialog, (ViewGroup)
			 * findViewById(R.id.layout_root));
			 * 
			 * TextView text = (TextView) layout.findViewById(R.id.text);
			 * text.setText("Hello, this is a custom dialog!"); ImageView image
			 * = (ImageView) layout.findViewById(R.id.image);
			 * image.setImageBitmap(wallpapers.get(pos).getThumb());
			 * 
			 * 
			 * 
			 * builder = new AlertDialog.Builder(Main.this);
			 * builder.setView(layout); alertDialog = builder.create();
			 * alertDialog.show();
			 */
			update_prefs();

			Log.d("Pref", pref_tap_action);
			Log.d("Pref", pref_tap_action);
			Log.d("Pref", pref_tap_action);
			Log.d("Pref", pref_tap_action);
			Log.d("Pref", pref_tap_action);
			Log.d("Pref", pref_tap_action);

			if ("set".equals(pref_tap_action)) {
				new AlertDialog.Builder(v.getContext())
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("Set Wallpaper?")
						.setMessage("Download and Set Wallpaper?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										setWallpaper(gpos);

									}

								}).setNegativeButton("No", null).show();
			} else if ("download".equals(pref_tap_action)) {
				downloadWallpaper(pos);

			} else if ("comment".equals(pref_tap_action)) {
				open_comment(pos);

			} else if ("open".equals(pref_tap_action)) {
				open_image(pos);

			}
		}

	};

	public void search() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		alert.setTitle("Search");
		alert.setView(input);
		alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString().trim();
				currentFeed = "http://www.reddit.com/r/" + pref_active_reddits
						+ "/search.json?q=" + value + "&restrict_sr=on";

				dont_promt_search = true;
				streamSpinner.setSelection(6);// search gets trigger
												// again from this.
												// but we set this
												// for calls from
												// search button

				refresh();
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
						dont_refresh = true;
						stream_ui_curpos = stream_ui_lastpos;
						streamSpinner.setSelection(stream_ui_curpos);
					}
				});
		alert.show();

	}

	public void refresh() {

		if (!dont_refresh) {// the currentFeed check
							// is for the fact i
							// havent made this work

			
			
			update_prefs();

			dialog = ProgressDialog.show(Main.this, "",
					"Loading. Please wait...", true);

			new Thread(new Runnable() {
				public void run() {

					wallpapers.clear();

					if ("local".equals(currentFeed)) {
						wallpapers.addAll(mDB.getAllWp());

					} else {
						try {
							JSONObject jo = GetReddit.getReddit(currentFeed+"limit="+pref_count);
							JSONArray children = jo.getJSONArray("children");
							int s = children.length();

							for (int x = 0; x < s; x++) {
								JSONObject jox = children.getJSONObject(x);
								JSONObject joxd = jox.getJSONObject("data");

								String title = joxd.getString("title");
								String url = joxd.getString("url");
								String name = joxd.getString("name");
								String thumbnail = joxd.getString("thumbnail");
								String perma = joxd.getString("permalink");
								String author = joxd.getString("author");
								int score = joxd.getInt("score");
								int num_comm = joxd.getInt("num_comments");

								Boolean is_self = joxd.getBoolean("is_self");

								if (!is_self) {

									if (url.startsWith("http://i.imgur.com/")
											||
											// i.imgur.com
											// if just on imgur.com
											(url.startsWith("http://imgur.com/") && (url
													.endsWith(".jpg") || url
													.endsWith(".png")))) {
										Wallpaper newWp = new Wallpaper(title,
												url, thumbnail, perma, name,
												author, score, num_comm);

										File filex = new File(
												Constants.EXTERNAL_STORAGE,
												name + ".jpg");
										if (filex.exists()) {
											newWp.setLocalURI(filex
													.getAbsolutePath());
											// Log.d("filez","we have it");
										} else {
											// Log.d("filez","we dont!");
										}

										wallpapers.add(newWp);
									}
								}

							}

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();

						}
					}
					dialog.dismiss();
					handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
				}
			}).start();
		} else {
			dont_refresh = false;

		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case NOTIFY_DATASET_CHANGED:
				adapt.notifyDataSetChanged();
			default:
				break;
			}
		}
	};

	public void update_prefs() {
		pref_count = Integer.parseInt(mPrefs.getString("pref_count", "50"));
		pref_use_compact = mPrefs.getBoolean("pref_use_compact", true);
		pref_tap_action = mPrefs.getString("pref_tap_action", "set");
		pref_active_reddits = mPrefs
				.getString("pref_active_reddits", "redwall");

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_REFRESH, 0, "Refresh");
		menu.add(0, MENU_SEARCH, 0, "Search");

		menu.add(0, MENU_QUIT, 0, "Quit");
		menu.add(0, MENU_ABOUT, 0, "About / FAQ");

		menu.add(0, MENU_PREF, 0, "Options");

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			refresh();
			return true;
		case MENU_SEARCH:
			search();
			return true;

		case MENU_ABOUT:
			open_about();
			return true;

		case MENU_QUIT:
			this.finish();
			return true;

		case MENU_PREF:
			Intent intent = new Intent().setClass(this, Preferences.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			search();

			return false;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (!Constants.EXTERNAL_STORAGE.exists())
			Constants.EXTERNAL_STORAGE.mkdirs();

		if (currentapiVersion >= 5) {
			wallpaperManager = WallpaperManager.getInstance(this);
		}

		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		update_prefs();

		mDB = new DBHelper(this);

		list = (ListView) this.findViewById(R.id.list);
		int layoutID = R.layout.list_item;
		adapt = new WallpaperAdapter(this, layoutID, wallpapers);
		list.setAdapter(adapt);
		list.setOnItemClickListener(clickListener);
		list.setOnCreateContextMenuListener(cmListener);

		// refresh();
		// Refresh is now triggered by spinner in action bar upon startup.

		cmdActionBarRefresh = (ImageButton) this
				.findViewById(R.id.btn_title_refresh);
		cmdActionBarRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				cmdActionBarRefresh.setVisibility(View.GONE);

				ProgressBar progressbar = (ProgressBar) findViewById(R.id.title_refresh_progress);
				progressbar.setVisibility(View.VISIBLE);

				refresh();

				// How do we make the app pause until the refresh is complete?
				// startActivityForReuslt doesn't work as we pass a "void", not
				// an "Intent".

				cmdActionBarRefresh.setVisibility(View.VISIBLE);
				progressbar.setVisibility(View.GONE);
			}
		});

		streamSpinner = (Spinner) findViewById(R.id.spinner_stream);
		ArrayAdapter<CharSequence> streamSpinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.streams_array,
						R.layout.actionbar_spinner_item);
		streamSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		streamSpinner.setAdapter(streamSpinnerAdapter);

		streamSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						// TODO Auto-generated method stub

						// Toast.makeText(parent.getContext(),
						// Integer.toString(pos), Toast.LENGTH_LONG)
						// .show();

						stream_ui_lastpos = stream_ui_curpos;
						stream_ui_curpos = pos;

						switch (pos) {
						case 0: {
							currentFeed = "http://www.reddit.com/r/"
									+ pref_active_reddits + "/.json?";
									
							refresh();
							break;
						}
						case 1: {
							currentFeed = "http://www.reddit.com/r/"
									+ pref_active_reddits
									+ "/new.json?sort=new&";
							refresh();
							break;
						}

						case 2: {
							currentFeed = "http://www.reddit.com/r/"
									+ pref_active_reddits
									+ "/top.json?t=week&";
							refresh();
							break;
						}
						case 3: {
							currentFeed = "http://www.reddit.com/r/"
									+ pref_active_reddits
									+ "/top.json?t=month&";
							refresh();
							break;
						}
						case 4: {
							currentFeed = "http://www.reddit.com/r/"
									+ pref_active_reddits
									+ "/top.json?t=year&";
							refresh();
							break;
						}
						case 5: {
							currentFeed = "local";
							refresh();
							break;
						}
						case 6: {

							if (!dont_promt_search) {
								search();
							} else {
								dont_promt_search = false;
							}
							break;
						}
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
						Toast.makeText(Main.this, "??", Toast.LENGTH_LONG)
								.show();
					}

				});
		pref_frist_run = mPrefs.getBoolean(Constants.SETTING_KEY_RUN, true);
		// first run stuff
		if (pref_frist_run) {

			// open_about();
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("v22-Konichiwa")
					.setMessage(
							"We have added a lot of great features (Search, Local Files, Settings?lol).  But some of these require a reset of the /redwall directory on your sd card.  To have wallpapers show up in the local list they must be re-downloaded. ")
					.setPositiveButton("Empty Dir",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

									File[] files = Constants.EXTERNAL_STORAGE
											.listFiles();
									for (int i = 0; i < files.length; i++) {
										files[i].delete();
									}
									
									
									SharedPreferences.Editor edit = mPrefs.edit();
									edit.putBoolean(Constants.SETTING_KEY_RUN, false);
									edit.commit();
								}

							}).show();


		}
	}

}