package com.kudzu.android.redwall.pro;


import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class WallpaperAdapter extends ArrayAdapter<Wallpaper> {
	int resource;
	ImageLoader imageLoader;
	Context ctx;
	public WallpaperAdapter(Context _context, int _resource, List<Wallpaper> _items) {
		super(_context, _resource, _items);
		resource = _resource;
		ctx = _context;
		imageLoader = new ImageLoader(ctx);
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout newsView;
		Wallpaper item = getItem(position);
		
		String title = item.getTitle();
		String url = item.getImg();
		String thumb = item.getThumb();
		String author = item.getSubmitor();
		
		//i know how dirty this looks
		//but getting the relative layout to work like i needed
		//        ##### TITLE######
		//   IMG
		//        #SCORE#       #COMMENTS#
		//				    /\
		//                problem
		String other = item.getScore() + " points, "+item.getComments()+" comments, by " +author;


		if (convertView == null) {
			newsView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
			vi.inflate(resource, newsView, true);
		} else {
			newsView = (LinearLayout) convertView;
		}

		
		TextView txtTitle = (TextView)newsView.findViewById(R.id.txtTitle);
		TextView txtOther = (TextView)newsView.findViewById(R.id.txtOther);
		ImageView imgView = (ImageView)newsView.findViewById(R.id.thumb);

		imageLoader.DisplayImage(this,thumb, ctx, imgView);
		
		
		//imgView.setImageBitmap(thumb);
		txtTitle.setText(title);
		txtOther.setText(other);
		return newsView;
	}
}