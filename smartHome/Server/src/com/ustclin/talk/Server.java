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
	private int BaudRates = 115200; // ������
	private int DataBits = 8; // ����λ
	private int StopBits = 1; // ֹͣλ
	private int ParityBits = SerialPort.PARITY_NONE; // ��żУ��λ
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
	String str1 = "vfw:Logitech   USB   Video   Camera:0"; // ��ȡUSB����ͷ���ַ���
	String str2 = "vfw:Microsoft WDM Image Capture (Win32):0"; // ��ȡ��������ͷ���ַ���
	static Socket socket = null;
	@SuppressWarnings("restriction")
	public Server() {
		setTitle("���ܼҾӿ���ϵͳ");
		setBounds(500, 100, 600, 500);
		final JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
//		ta_info.setBackground(Color.LIGHT_GRAY);
//		ta_info.setFont(new Font("����", 1, 15));
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
		captureDeviceInfo = CaptureDeviceManager.getDevice(str); // ������õ�����Ƶ����
		mediaLocator = new MediaLocator("vfw://0"); // ��������Ƶ��ַ
		imagePanel = new ImagePanel();
		try {
			player = Manager.createRealizedPlayer(mediaLocator);
			player.start();
			Component comp;
			Component comp1;
			if ((comp = player.getVisualComponent()) != null)
				add(comp, BorderLayout.NORTH);
		} catch (NoPlayerException e) {
			System.out.println("����1");
		} catch (CannotRealizeException e) {
			System.out.println("����2");
		} catch (IOException e) {
			System.out.println("����3");
		}
		
		add(imagePanel, BorderLayout.CENTER);
		
		
	}
	
	public void print(Graphics g) {
		super.print(g);
		g.setColor(new Color(255, 0, 0));
		g.drawLine(0, 0, 100, 100);
	}
	//���ն���
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
				System.out.println("����4");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("����5");
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
					System.out.println("��Ϣ��"+name);
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
								outputStream.write(name.getBytes()); // �����L1#,#��������ַ�
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
	/* ���ô��ڲ��� */
	public void serialPort_set() {
		/* �򿪴��� */
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
					try { /* ����·�϶�ȡ������ */
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
								out.println("��ǰ���¶��ǣ�" + ca[5] + ca[6]);
								out.println("��ǰ��ʪ���ǣ�" + ca[17] + ca[18]);
								break;
							case 'h':
								switch (ca[5]) {
								case 's':
									out.println("��������");
									break;
								case 'p':
									out.println("��������");
									break;
								case 'i':
									out.println("��ǰ��ʪ���ǣ�" + ca[17] + ca[18]);
									break;
								default:
									break;
								}
							case 'n':
								switch (ca[3]) {
								case 'p':
									out.println("����û��");
									break;
								case 'l':
									out.println("����ǿ����");
									break;
								case 's':
									out.println("û�з�������");
									break;
								default:
									break;
								}
							case 'P':
//									ActionP(); // ����
								out.println("�������쳣���Ѿ��������ձ���");
								break;
							case 'W':
//								ActionP(); // ����
								out.println("���ﴰ���쳣���Ѿ��������ձ���");
								break;
							case 'o':
								out.println("�������ģʽ");
								break;
							case 'i':
								out.println("�ر����ģʽ");
								break;
							case 's':
								out.println("����˯��ģʽ");
								break;
							case 'r':
								out.println("�ر�˯��ģʽ");
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
		/* ����������������,���������¼� */
		serialPort.notifyOnDataAvailable(true);
	}
//����ͼƬ���ֻ���
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
	        System.out.println("���ͳɹ�");
		} catch (Exception e) {
		}
		
	}
	
	public static void main(String[] args) {
		Server m = new Server();
		m.setVisible(true);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(8858);
//			ta_info.append("�ȴ��ͻ�������........" + "\n");
			System.out.println("�ȴ��ͻ�������........" + "\n");
			while (true) {
				Socket accept = serverSocket.accept();
				socketList.add(accept);
//				ta_info.append("���ӳɹ�!\n");
				System.out.println("���ӳɹ�!\n");
				new Thread(new Server(accept)).start();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				serverSocket.close();
//				ta_info.append("----> �������ر�!" + "\n");
				System.out.println("----> �������ر�!" + "\n");
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