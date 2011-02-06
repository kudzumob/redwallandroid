package com.kudzu.android.redwall.pro;

import android.graphics.Bitmap;

public class Wallpaper {
	private String title;
	private String img;
	private String thumb;
	private String src;
	
	
	
	public Wallpaper(String title,String img,String thumb,String src){
		this.title = title;
		this.img = img;
		this.thumb = thumb;		
		this.setSrc(src);	
	}
	
	
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getImg() {
		return img;
	}
	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
	public String getThumb() {
		return thumb;
	}


	public void setSrc(String src) {
		this.src = src;
	}


	public String getSrc() {
		return src;
	}



}
