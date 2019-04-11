package com.example.qq;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserService {

	private DBconnection dbHelper;
	
	public UserService(Context context) {
		dbHelper=new DBconnection(context);
	}
	
	public boolean login(String username,String password)
	{
		SQLiteDatabase sdb=dbHelper.getReadableDatabase();
		String sql="select * from user where username=? and password=?";
		Cursor cursor=sdb.rawQuery(sql, new String[]{username,password});
		if(cursor.moveToFirst())
		{
			cursor.close();
			sdb.close();
			return true;
		}
		sdb.close();
		return false;
	}
	public boolean register(User user)
	{
		SQLiteDatabase sdb=dbHelper.getReadableDatabase();
		String sql="insert into user(username,password) values(?,?)";
		Object obj[]={user.getUsername(),user.getPassword()};
		sdb.execSQL(sql,obj);
		sdb.close();
		return true;
	}
}
