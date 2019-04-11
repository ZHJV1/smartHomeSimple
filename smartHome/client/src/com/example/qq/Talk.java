package com.example.qq;

public class Talk {
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String TIME = "time";
	public static final String PS = "ps";	
	public static final String MESS = "mess";	
	private String id;
	private String name;
	private String time;
	private int ps;
	private String mess;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getPs() {
		return ps;
	}
	public void setPS(int ps) {
		this.ps = ps;
	}
	public String getMess() {
		return mess;
	}
	public void setMess(String mess) {
		this.mess = mess;
	}
}
