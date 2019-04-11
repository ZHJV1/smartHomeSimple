package com.example.qq;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
public class MainActivity extends Activity implements OnClickListener {
	private ListView mServerMsg;
	private EditText editText;
	private Button button1;
	private Button buttonimage;
	private MyHandler myHandler;
	private Socket socket = null;
	private ListAdapter listAdapter;
	private List<Talk> Talklist = new ArrayList<Talk>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setTitle("室内温湿度检测系统");
		editText = (EditText) findViewById(R.id.message);
		mServerMsg = (ListView) findViewById(R.id.talked);
		buttonimage = (Button) findViewById(R.id.goimageview);
		listAdapter = new ListAdapter();
		mServerMsg.setAdapter(listAdapter);
		registerForContextMenu(mServerMsg);
		button1 = (Button) findViewById(R.id.send_message);
		myHandler = new MyHandler();
		button1.setOnClickListener(this);
		buttonimage.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		//获取文本框的内容
		if (v.getId() == R.id.send_message) {
			Runnable r2 = new Runnable() {
				@Override
				public void run() {
					Message msg = myHandler.obtainMessage();
					//获取文本框的内容
					String mess = editText.getText().toString();
					Bundle bundle = new Bundle();
					bundle.putString("mess", mess);
					msg.what = 0;
					msg.setData(bundle);
					myHandler.sendMessage(msg);
				}
			};
			new Thread(r2).start();
		}
		
		//连接服务器，
		if (v.getId() == R.id.goimageview) {
			Runnable r1 = new Runnable() {
				@Override
				public void run() {
					initSocket();
					ClientThread ct = new ClientThread(socket, "Client");
					new Thread(ct).start();
					Message msg = myHandler.obtainMessage();
					msg.what = 4;
					myHandler.sendMessage(msg);
				}
			};
			new Thread(r1).start();
			Runnable r3 = new Runnable() {
				@Override
				public void run() {
					Message msg = myHandler.obtainMessage();
					sendData("Open");
					msg.what = 3;
					myHandler.sendMessage(msg);
				}
			};
			new Thread(r3).start();
		}
	}
	
	private void initSocket() {
		try {
			// 连接不上的原因主要是：IP地址没有设置，端口号不一致或者是端口
			socket = new Socket("192.168.191.1", 8858);
			socket.setKeepAlive(true);
			socket.setSoTimeout(10 * 60 * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				final String mess = (String) msg.getData().get("mess");
				if (mess.equals("")) {
					return;
				} else {
					new Thread(new Runnable() {
						@Override
						public void run() {
							sendData(mess);
						}
					}).start();
					editText.setText("");
				}
				break;
			case 1:
				String name_ = (String) msg.getData().get("name_");
				Talk talk = new Talk();
				talk.setName(name_);
				Talklist.add(talk);
				mServerMsg.setAdapter(new ListAdapter());
			case 3:
				buttonimage.setVisibility(View.GONE);
			default:
				break;
			}
		};
	};

	private void sendData(String mess) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(socket.getOutputStream(), "UTF-8")),
					true);
			out.println(mess);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void CLOSE_DATA() {
		Runnable r4 = new Runnable() {
			@Override
			public void run() {
				Message msg = myHandler.obtainMessage();
				msg.what = 2;
				myHandler.sendMessage(msg);
			}
		};
		new Thread(r4).start();
	}

	private void CONNECTION_CLOSE() {
		try {
			if (null != socket) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class ClientThread implements Runnable {
		private Socket socket = null;
		private String name;

		public ClientThread(Socket socket, String threadName) {
			this.socket = socket;
			this.name = threadName;
		}

		@Override
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream(), "utf-8"));

				while (true) {
					String name = null;
					try {
						if (!socket.isClosed()) {
							name = in.readLine();
							Message msg = myHandler.obtainMessage();
							Bundle b = new Bundle();
							b.putString("name_", name);
							msg.setData(b);
							msg.what = 1;
							myHandler.sendMessage(msg);
						}
						Thread.sleep(1000);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ListAdapter extends BaseAdapter {
		public ListAdapter() {
			super();
		}

		public int getCount() {
			return Talklist.size();
		}

		public Object getItem(int postion) {
			return postion;
		}

		public long getItemId(int postion) {
			return postion;
		}

		public View getView(final int postion, View view, ViewGroup parent) {
			view = getLayoutInflater().inflate(R.layout.listview, null);
			TextView tv1 = (TextView) view.findViewById(R.id.tv1);
			tv1.setText(Talklist.get(postion).getName());
			return view;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CLOSE_DATA();
		CONNECTION_CLOSE();
	}
}