package com.kudzu.android.redwall.pro;



public class Wallpaper {
	private String title;
	private String img;
	private String thumb;
	private String src;
	private String localURI;
	private String name;



	public Wallpaper(String title,String img,String thumb,String src,String name){
		this.title = title;
		this.img = img;
		this.thumb = thumb;		
		this.src = src;	
		this.name = name;
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

	public void setLocalURI(String localURI){
		this.localURI = localURI;
	}

	public String getLocalURI() {
		return localURI;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}



}