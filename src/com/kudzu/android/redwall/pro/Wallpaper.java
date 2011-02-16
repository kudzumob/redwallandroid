package com.kudzu.android.redwall.pro;



public class Wallpaper {
	private String title;
	private String img;
	private String thumb;
	private String src;
	private String localURI;
	private String name;

	private String submitor;
	private int score;
	private int comments;

	public boolean isLocal = false;
	
	public Wallpaper(String title,String img,String thumb,String src,String name,String submitor,int score,int comments){
		this.title = title;
		this.img = img;
		this.thumb = thumb;		
		this.src = src;	
		this.name = name;		
		this.submitor=submitor;
		this.score=score;
		this.comments= comments;
	}

	public Wallpaper(String title,String local_uri,String src,String thumb){
		this.isLocal = true;
		this.title = title;
		this.localURI = local_uri;
		this.src = src;		
		this.thumb = thumb;		
		
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

	public void setSubmitor(String submitor) {
		this.submitor = submitor;
	}

	public String getSubmitor() {
		return submitor;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public int getComments() {
		return comments;
	}



}