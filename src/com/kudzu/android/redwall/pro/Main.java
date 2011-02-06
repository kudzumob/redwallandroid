package com.kudzu.android.redwall.pro;

import java.io.IOException;
import java.io.InputStream;

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
import android.os.Handler;
import android.os.Message;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity {
	protected static final int MENU_COMMENT = Menu.FIRST + 1;
	protected static final int MENU_VIEW = Menu.FIRST + 2;
	protected static final int MENU_SET = Menu.FIRST + 3;

	protected static final int MENU_REFRESH = Menu.FIRST + 10;
	protected static final int MENU_QUIT = Menu.FIRST + 11;
	protected static final int MENU_ABOUT = Menu.FIRST + 12;
	final int NOTIFY_DATASET_CHANGED = 1;

	private String LOCAL_PATH = "/sdcard/.redwall";

	ProgressDialog dialog;

	WallpaperAdapter adapt;
	ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();

	ListView list;

	OnCreateContextMenuListener cmListener = new OnCreateContextMenuListener() {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			// TODO Auto-generated method stub
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

	public void setWallpaper(int pos) {

		String url = wallpapers.get(pos).getImg();
		try {
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
					url).getContent());
			getApplicationContext().setWallpaper(bitmap);
			Toast.makeText(Main.this, "Wallpaper Updated", Toast.LENGTH_LONG)
					.show();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void refresh() {
		wallpapers.clear();
		try {
			JSONObject jo = GetReddit
					.getReddit("http://www.reddit.com/r/redwall.json");
			JSONArray children = jo.getJSONArray("children");
			int s = children.length();

			for (int x = 0; x < s; x++) {
				JSONObject jox = children.getJSONObject(x);
				JSONObject joxd = jox.getJSONObject("data");
				String title = joxd.getString("title");

				Boolean is_self = joxd.getBoolean("is_self");

				if (!is_self) {

					/*
					Bitmap thumb = BitmapFactory
							.decodeStream((InputStream) new URL(joxd
									.getString("thumbnail")).getContent());
					 */
					
					Wallpaper newWp = new Wallpaper(title,
							joxd.getString("url"), joxd.getString("thumbnail"),
							joxd.getString("permalink"));
					wallpapers.add(newWp);

				}

			}
			// setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,
			// DATA));
			// getListView().setTextFilterEnabled(true);

			// txtTop.setText("");
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
		menu.add(0, MENU_ABOUT, 0, "About / FAQ");
		menu.add(0, MENU_QUIT, 0, "Quit");
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			dialog = ProgressDialog.show(Main.this, "",
					"Loading. Please wait...", true);
			new Thread(new Runnable() {
				public void run() {
					refresh();
					dialog.dismiss();
					handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
				}
			}).start();
			return true;

		case MENU_ABOUT:
			open_about();
			return true;

		case MENU_QUIT:
			this.finish();
			return true;
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);


		
		
		list = (ListView) this.findViewById(R.id.list);
		int layoutID = R.layout.list_item;
		adapt = new WallpaperAdapter(this, layoutID, wallpapers);
		list.setAdapter(adapt);
		list.setOnItemClickListener(clickListener);
		list.setOnCreateContextMenuListener(cmListener);

		
		
		dialog = ProgressDialog.show(Main.this, "", "Loading. Please wait...",
				true);
		new Thread(new Runnable() {
			public void run() {
				refresh();
				dialog.dismiss();
				handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
			}
		}).start();


	}

}