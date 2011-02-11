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

	private static LayoutInflater inflater = null;

	public WallpaperAdapter(Context _context, int _resource,
			List<Wallpaper> _items) {
		super(_context, _resource, _items);
		resource = _resource;
		ctx = _context;

		inflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		imageLoader = new ImageLoader(ctx);

	}

	public static class ViewHolder {
		public TextView txtTitle;
		public TextView txtOther;
		public ImageView imgThumb;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;
		if (convertView == null) {
			vi = inflater.inflate(resource, null);
			holder = new ViewHolder();
			holder.txtTitle = (TextView) vi.findViewById(R.id.txtTitle);
			holder.txtOther = (TextView) vi.findViewById(R.id.txtOther);
			holder.imgThumb = (ImageView) vi.findViewById(R.id.thumb);
			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();

		Wallpaper item = getItem(position);
		String title = item.getTitle();
		String url = item.getImg();
		String thumb = item.getThumb();
		String author = item.getSubmitor();
		String other = item.getScore() + " points, " + item.getComments()
				+ " comments, by " + author;

		
		holder.imgThumb.setTag(thumb);
		imageLoader.DisplayImage(this, thumb, ctx, holder.imgThumb);
		
		// imgView.setImageBitmap(thumb);
		holder.txtTitle.setText(title);
		holder.txtOther.setText(other);
		return vi;
	}
}