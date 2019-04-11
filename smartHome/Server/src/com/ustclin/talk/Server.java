package com.ustclin.talk;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.TooManyListenersException;
import javax.media.Buffer;
import javax.media.CannotRealizeException;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.control.FrameGrabbingControl;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;



public class Server extends JFrame implements Runnable {
	private CommPortIdentifier portId;
	private SerialPort serialPort;
	private InputStream inputStream;
	private boolean commBeOpened = false;
	private OutputStream outputStream;
	static Server commFrame = null;
	private int BaudRates = 115200; // 波特率
	private int DataBits = 8; // 数据位
	private int StopBits = 1; // 停止位
	private int ParityBits = SerialPort.PARITY_NONE; // 奇偶校验位
	public static JTextArea ta_info = new JTextArea();
	private static ArrayList<Socket> socketList = new ArrayList<Socket>();
	private boolean temo;
	private CaptureDeviceInfo deviceInfo = null;
	private Component component = null;
	private JPanel vedioPanel = null;
	private CaptureDeviceInfo captureDeviceInfo = null;
	private MediaLocator mediaLocator = null;
	private static ImagePanel imagePanel = null;
	private static Buffer buffer = null;
	private VideoFormat videoFormat = null;
	private static BufferToImage bufferToImage = null;
	private static Image image = null;
	private static Player player = null;
	String str1 = "vfw:Logitech   USB   Video   Camera:0"; // 获取USB摄像头的字符串
	String str2 = "vfw:Microsoft WDM Image Capture (Win32):0"; // 获取本地摄像头的字符串
	static Socket socket = null;
	@SuppressWarnings("restriction")
	public Server() {
		setTitle("智能家居控制系统");
		setBounds(500, 100, 600, 500);
		final JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
//		ta_info.setBackground(Color.LIGHT_GRAY);
//		ta_info.setFont(new Font("黑体", 1, 15));
		scrollPane.setViewportView(ta_info);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				commBeOpened = false;
				try {
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if (serialPort != null) {
					serialPort.close();
				}
				try {
					if (null != socket) {
						socket.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				setVisible(false);
				System.exit(0);
			}
		});
		String str="vfw:Microsoft WDM Image Capture (Win32):0";
		captureDeviceInfo = CaptureDeviceManager.getDevice(str); // 这里放置的是视频驱动
		mediaLocator = new MediaLocator("vfw://0"); // 这里是视频地址
		imagePanel = new ImagePanel();
		try {
			player = Manager.createRealizedPlayer(mediaLocator);
			player.start();
			Component comp;
			Component comp1;
			if ((comp = player.getVisualComponent()) != null)
				add(comp, BorderLayout.NORTH);
		} catch (NoPlayerException e) {
			System.out.println("错误1");
		} catch (CannotRealizeException e) {
			System.out.println("错误2");
		} catch (IOException e) {
			System.out.println("错误3");
		}
		
		add(imagePanel, BorderLayout.CENTER);
		
		
	}
	
	public void print(Graphics g) {
		super.print(g);
		g.setColor(new Color(255, 0, 0));
		g.drawLine(0, 0, 100, 100);
	}
	//拍照动作
	public static void ActionP(){
		int sj=(int)(Math.random()*10000);
		FrameGrabbingControl fgc = (FrameGrabbingControl) player
					.getControl("javax.media.control.FrameGrabbingControl");
			buffer = fgc.grabFrame();
			bufferToImage = new BufferToImage((VideoFormat) buffer.getFormat());
			image = bufferToImage.createImage(buffer);
			imagePanel.setImage(image);
			saveImage(image, "F:Test/"+sj+".jpg");
		}
		public static void saveImage(Image image, String path) {
			BufferedImage bi = new BufferedImage(image.getWidth(null), image
					.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = bi.createGraphics();
			g2.drawImage(image, null, null);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(path);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			JPEGImageEncoder je = JPEGCodec.createJPEGEncoder(fos);
			JPEGEncodeParam jp = je.getDefaultJPEGEncodeParam(bi);
			jp.setQuality(0.5f, false);
			je.setJPEGEncodeParam(jp);
			try {
				je.encode(bi);
				fos.close();
			} catch (ImageFormatException e) {
				// TODO Auto-generated catch block
				System.out.println("错误4");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("错误5");
			}
		}
	public boolean isTemo() {
		return temo;
	}

	public void setTemo(boolean temo) {
		this.temo = temo;
	}

	public Server(Socket socket) {
		this.socket = socket;
	}

	public void OpenPort() {
		if (commBeOpened) {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			serialPort.removeEventListener();
			serialPort.notifyOnDataAvailable(false);
			serialPort.close();
			commBeOpened = false;
		} else {
			serialPort_set();
			commBeOpened = true;
		}
	}


	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), "GBK"));
			while (true) {
				String name = null;
				if (!socket.isClosed()) {
					try {
						name = in.readLine();
					} catch (Exception e) {
						e.printStackTrace();
					}
//					ta_info.append(name + "\n");
					System.out.println("消息是"+name);
					if (name.equals("k")) {
						ActionP();
					}
					if (name.startsWith("Open")) {
						if (commBeOpened) {
							if (outputStream != null) {
								try {
									outputStream.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
							serialPort.removeEventListener();
							serialPort.notifyOnDataAvailable(false);
							serialPort.close();
							commBeOpened = false;
						} else {
							commBeOpened = true;
							serialPort_set();
							commBeOpened = true;
						}
						System.out.print("qqq");
					} else {
						if (commBeOpened) {
							try {
								outputStream.write(name.getBytes()); // 输出流L1#,#代表结束字符
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/* 设置串口参数 */
	public void serialPort_set() {
		/* 打开串口 */
		try {
			portId = CommPortIdentifier.getPortIdentifier("COM3");
			serialPort = (SerialPort) portId.open("Comm", 8848);
			inputStream = serialPort.getInputStream();
			commBeOpened = true;
		} catch (NoSuchPortException e1) {
			System.out.print(e1);
			commBeOpened = false;
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (PortInUseException e) {
			commBeOpened = false;
			return;
		}
		try {
			serialPort.setSerialPortParams(BaudRates, DataBits, StopBits,
					ParityBits);
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			outputStream = serialPort.getOutputStream();
		} catch (UnsupportedCommOperationException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			serialPort.addEventListener(new SerialPortEventListener() {
				public void serialEvent(SerialPortEvent event) {
					byte[] readBuffer = new byte[1024];
					int a = 0;
					try { /* 从线路上读取数据流 */
						while (inputStream.available() > 0) {
							a = inputStream.read(readBuffer);
						}
						String str = new String(readBuffer).substring(0, a);
//						ta_info.append(str);
						char[] ca = str.toCharArray();
						for (Socket ss : Server.socketList) {
							PrintWriter out = new PrintWriter(
									new BufferedWriter(new OutputStreamWriter(
											ss.getOutputStream(), "utf-8")),
									true);
							switch (ca[0]) {
							case 't':
								out.println("当前的温度是：" + ca[5] + ca[6]);
								out.println("当前的湿度是：" + ca[17] + ca[18]);
								break;
							case 'h':
								switch (ca[5]) {
								case 's':
									out.println("发现烟雾：");
									break;
								case 'p':
									out.println("家里有人");
									break;
								case 'i':
									out.println("当前的湿度是：" + ca[17] + ca[18]);
									break;
								default:
									break;
								}
							case 'n':
								switch (ca[3]) {
								case 'p':
									out.println("家里没人");
									break;
								case 'l':
									out.println("光照强度弱");
									break;
								case 's':
									out.println("没有发现烟雾");
									break;
								default:
									break;
								}
							case 'P':
//									ActionP(); // 拍照
								out.println("家里有异常，已经进行拍照保存");
								break;
							case 'W':
//								ActionP(); // 拍照
								out.println("家里窗户异常，已经进行拍照保存");
								break;
							case 'o':
								out.println("开启外出模式");
								break;
							case 'i':
								out.println("关闭外出模式");
								break;
							case 's':
								out.println("开启睡眠模式");
								break;
							case 'r':
								out.println("关闭睡眠模式");
								break;
							default:
								break;
								
							}
						}

					} catch (IOException e) {
					}
				}
			});
		} catch (TooManyListenersException e) {
		}
		/* 侦听到串口有数据,触发串口事件 */
		serialPort.notifyOnDataAvailable(true);
	}
//发送图片到手机端
	public static void sendImage(){
		try {
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());    
	        FileInputStream fis = new FileInputStream("F:/Test/2748.jpg");    
	        int size = fis.available();  
	        System.out.println("size = "+size);  
	        byte[] data = new byte[size];    
	        fis.read(data);    
	        dos.writeInt(size);    
	        dos.write(data);    
	        dos.flush();    
	        System.out.println("发送成功");
		} catch (Exception e) {
		}
		
	}
	
	public static void main(String[] args) {
		Server m = new Server();
		m.setVisible(true);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(8858);
//			ta_info.append("等待客户端连接........" + "\n");
			System.out.println("等待客户端连接........" + "\n");
			while (true) {
				Socket accept = serverSocket.accept();
				socketList.add(accept);
//				ta_info.append("连接成功!\n");
				System.out.println("连接成功!\n");
				new Thread(new Server(accept)).start();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				serverSocket.close();
//				ta_info.append("----> 服务器关闭!" + "\n");
				System.out.println("----> 服务器关闭!" + "\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
class ImagePanel extends Panel {
	public Image myimg = null;

	public ImagePanel() {
		setLayout(null);
		setSize(320, 240);
	}
	public void setImage(Image img) {
		this.myimg = img;
		repaint();
	}

	public void paint(Graphics g) {
		if (myimg != null) {
			g.drawImage(myimg, 0, 0, this);
		}
	}
	
	
	
	
}