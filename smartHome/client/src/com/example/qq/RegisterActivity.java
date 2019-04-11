package com.example.qq;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	EditText username;
	EditText password;
	EditText password_;
	Button register;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		setTitle("用户注册");
		username=(EditText) findViewById(R.id.usernameRegister);
		password=(EditText) findViewById(R.id.passwordRegister);
		password_=(EditText) findViewById(R.id.passwordRegister_);
		register=(Button) findViewById(R.id.Register);
        register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null!=username&&null!=password&&null!=password_)
				{
					String name=username.getText().toString();
					String pass=password.getText().toString();
					String pass_=password_.getText().toString();
					if("".equals(name.trim())||"".equals(pass.trim())||"".equals(pass_.trim())){
						Toast.makeText(RegisterActivity.this, "请输入注册信息!", Toast.LENGTH_LONG).show();
						return;
					}
					if (!pass.equals(pass_)) {
						Toast.makeText(RegisterActivity.this, "两次密码不一致!", Toast.LENGTH_LONG).show();
						return;
					}
					else{
					UserService userService=new UserService(RegisterActivity.this);
					User user=new User(name,pass);
					boolean flag = userService.register(user);
					if(flag)
					{
						Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();
						Intent intent = new Intent();				
	    			    intent.setClass(RegisterActivity.this,LoginActivity.class);
	    			    startActivity(intent);
					}
					else {
						Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_LONG).show();
					}
					}
				}
			}
		});
	}
}
