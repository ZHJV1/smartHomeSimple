package com.example.qq;

import android.R.bool;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    private EditText username;
    private EditText password;
    private Button login;
    private TextView register;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    	username = (EditText) findViewById(R.id.user);
    	password = (EditText) findViewById(R.id.password);
    	login = (Button) findViewById(R.id.input);
    	login.setOnClickListener(new OnClickListener() {
    		@Override
			public void onClick(View v) {
				if(null!=username||null!=password){
	    			String name = username.getText().toString();
	    			String pass = password.getText().toString();
	    			if("".equals(name.trim()))
	    			Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_LONG).show();
	    			UserService userService = new UserService(LoginActivity.this);
	    			boolean flag = userService.login(name, pass);
	    			
	    				if(flag){ 				
	    				Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();
	    				Bundle bundle = new Bundle();
	    				bundle.putString("name2", name);
	    				bundle.putString("pass2", pass);
	    				Intent intent = new Intent();				
	    			    intent.setClass(LoginActivity.this,MainActivity.class);
	    				intent.putExtras(bundle);			
	    			    startActivity(intent);
	    			    }else{
	    			    	
	    				Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
	    				}
	    			}
			}
		});
    	register = (TextView) findViewById(R.id.login_insert);
    	register.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    		Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
    		startActivity(intent);
    		}
    	});
    }
}
