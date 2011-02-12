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

import com.kudzu.android.redwall.pro.GetReddit.ApiException;
import com.kudzu.android.redwall.pro.GetReddit.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
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

	final int NOTIFY_DATASET_CHANGED = 1;

	private String LOCAL_PATH = "/redwall";

	private File EXTERNAL_STORAGE = new File(
			Environment.getExternalStorageDirectory() + LOCAL_PATH);

	int pref_count = 50;// reddits default

	String currentFeed = "http://www.reddit.com/r/redwall.json";

	ProgressDialog dialog;
	WallpaperAdapter adapt;
	ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();
	ListView list;
	ImageButton cmdActionBarRefresh;

	Spinner streamSpinner;
	int stream_ui_curpos = 0, stream_ui_lastpos = 0;
	boolean dont_refresh = false, dont_promt_search = false;

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
		open_web("http://www.reddit.com" + wallpapers.get(id).getSrc()
				+ ".compact");
	}

	private void open_about() {
		open_web("http://rootskudzumob.appspot.com/help.jsp?aid=ag1yb290c2t1ZHp1bW9icgwLEgRBcHBNGPnMAww");
	}

	public void downloadWallpaper(int pos) {
		if (wallpapers.get(pos).getLocalURI() == null) {
			String url = wallpapers.get(pos).getImg();
			try {
				Bitmap bitmap = BitmapFactory
						.decodeStream((InputStream) new URL(url).getContent());

				OutputStream fOut = null;
				File file = new File(EXTERNAL_STORAGE, wallpapers.get(pos)

				.getName() + ".jpg");

				fOut = new FileOutputStream(file);

				bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
				fOut.flush();
				fOut.close();

				// MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
				wallpapers.get(pos).setLocalURI(file.getAbsolutePath());

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
				getApplicationContext().setWallpaper(bitmap);
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

		}

	};

	public void search() {
		if (!dont_promt_search) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			final EditText input = new EditText(this);
			alert.setTitle("Search");
			alert.setView(input);
			alert.setPositiveButton("Go",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString().trim();
							currentFeed = "http://www.reddit.com/r/redwall/search.json?q="
									+ value + "&restrict_sr=on";

							streamSpinner.setSelection(6);// search gets trigger
															// again from this.
															// but we set this
															// for calls from
															// search button
							dont_promt_search = true;
							refresh();
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
							dont_refresh = true;
							stream_ui_curpos = stream_ui_lastpos;
							streamSpinner.setSelection(stream_ui_curpos);
						}
					});
			alert.show();
		} else {
			dont_promt_search = false;
		}
	}

	public void refresh() {

		if (!dont_refresh) {

			dialog = ProgressDialog.show(Main.this, "",
					"Loading. Please wait...", true);
			new Thread(new Runnable() {
				public void run() {

					wallpapers.clear();
					try {
						JSONObject jo = GetReddit.getReddit(currentFeed);
						JSONArray children = jo.getJSONArray("children");
						int s = children.length();

						for (int x = 0; x < s; x++) {
							JSONObject jox = children.getJSONObject(x);
							JSONObject joxd = jox.getJSONObject("data");
							String title = joxd.getString("title");
							String url = joxd.getString("url");

							Boolean is_self = joxd.getBoolean("is_self");

							if (!is_self) {

								if (url.startsWith("http://i.imgur.com/")
										||
										// i.imgur.com
										// if just on imgur.com
										(url.startsWith("http://imgur.com/") && (url
												.endsWith(".jpg") || url
												.endsWith(".png")))) {
									Wallpaper newWp = new Wallpaper(title, url,
											joxd.getString("thumbnail"), joxd
													.getString("permalink"),
											joxd.getString("name"), joxd
													.getString("author"), joxd
													.getInt("score"), joxd
													.getInt("num_comments"));
									wallpapers.add(newWp);
								}
							}

						}

					} catch (ApiException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

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
			this.finish();
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

		if (!EXTERNAL_STORAGE.exists())
			EXTERNAL_STORAGE.mkdirs();

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
							currentFeed = "http://www.reddit.com/r/redwall/.json?limit="
									+ pref_count;
							refresh();
							break;
						}
						case 1: {
							currentFeed = "http://www.reddit.com/r/redwall/new.json?sort=new&limit="
									+ pref_count;
							refresh();
							break;
						}
						case 2: {
							currentFeed = "http://www.reddit.com/r/redwall/top.json?t=day&limit="
									+ pref_count;
							refresh();
							break;
						}
						case 3: {
							currentFeed = "http://www.reddit.com/r/redwall/top.json?t=week&limit="
									+ pref_count;
							refresh();
							break;
						}
						case 4: {
							currentFeed = "http://www.reddit.com/r/redwall/top.json?t=month&limit="
									+ pref_count;
							refresh();
							break;
						}
						case 5: {
							currentFeed = "http://www.reddit.com/r/redwall/top.json?t=year&limit="
									+ pref_count;
							refresh();
							break;
						}
						case 6: {
							search();

							break;
						}
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub

					}

				});
	}
}