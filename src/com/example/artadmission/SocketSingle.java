package com.example.artadmission;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeoutException;

import com.example.artadmission.UI.MyUtils;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class SocketSingle extends Thread {
	// socketͨ��
	private static String RHOST = define_var.RHOST;
	private static int TPORT = Integer.valueOf(define_var.TPORT);
	private static Handler mHandler = null;
	public static Socket clientSocket = null;
	private BufferedReader in;
	private PrintWriter pw;
	private int repeatTimes = 0;
	Message msg;
	// ����
	public volatile boolean mConnectStop = false;
	private String str = null;
	private int timeout = 5000;
	// private Count_Timer timer = new Count_Timer(3000,1000);
	// ����ģʽ
	private static SocketSingle instance = null;

	public static synchronized SocketSingle getSocketSingleInstance(
			Handler Handler) {
		mHandler = Handler;
		if (instance == null) {
			instance = new SocketSingle();
		}
		return instance;
	}
	
	@Override
	public void run() 
	{
		
		RHOST = define_var.RHOST;
		TPORT = Integer.valueOf(define_var.TPORT);
		SocketAddress socketaddress = new InetSocketAddress(RHOST,TPORT);
		
		while(!mConnectStop)
		{
			System.out.println("����ѭ������-------");
			try{
				clientSocket = new Socket();
				clientSocket.connect(socketaddress, timeout);
				System.out.println("------------>�߳���������------>");
				in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream(), "GBK"));
				sendConnMsg();
				while (clientSocket.isConnected()&& !clientSocket.isClosed())
				{
					Log.e("socket","��������ǰ");
					
					if(((str = in.readLine()) == null)) 
					{
						Log.e("Socket","�������쳣�ر�");
						throw new Exception("�������쳣�ر�");
					}		
					Log.e("socket--->receviedMsg",str);
					receive_msg(str);		
				}
				System.out.println("end");
			}
			catch(Exception e)
			{
				sendDisconnMsg();
				closeSocket();
				System.out.println("�����쳣���  ��������������ֹ��� �޷�����");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		System.out.println("------------>�߳̽���");
							
	}

	//�������ݸ�������
	public void sendDataToServer(String str){
		if((clientSocket!=null)&&(clientSocket.isConnected()))
		{
			pw = null;
			try {
				pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
						clientSocket.getOutputStream(),"GBK")));
			} catch (Exception e) {  
				e.printStackTrace();  
			} 
			if(pw!=null)
			{
				pw.println(str);  
				System.out.println("--------���͵���Ϣstr=="+str);
				pw.flush();
			}	 
		}
		
	}
	//����������ʧ��
	private void sendDisconnMsg(){
		msg = new Message();
		msg.what = 1;
		mHandler.sendMessage(msg);
	}
	
	//���������ӳɹ�
	private void sendConnMsg(){
		msg = new Message();
		msg.what = 2;
		mHandler.sendMessage(msg);
	}	
	private void receive_msg(String receive) {
		if (receive.startsWith(define_var.connect_ok)) // ���ӳɹ�
		{
			message(define_var.conOKHandle, receive);
		} else if (receive.startsWith(define_var.connect_no)) // ����ʧ��
		{
			message(define_var.conNOHandle, receive);
		} else if (receive.startsWith(define_var.log_ok)) // ��¼�ɹ�
															// ������Ŀ|�������|��ί���|�û���ע��|�����ص�
		{
			message(define_var.logOKHandle, receive);
		} else if (receive.startsWith(define_var.log_no)) // ��¼ʧ��
		{
			message(define_var.logNOHandle, receive);
		} else if (receive.startsWith(define_var.newt)) // ����
		{
			message(define_var.newtHandle, receive);
		} else if (receive.startsWith(define_var.lose)) // ȱ��
		{
			message(define_var.loseHandle, receive);
		} else if (receive.startsWith(define_var.uploadScore_ack)) // �ϴ������Ļظ�
		{
			message(define_var.uploadScoreHandle, receive);
		} else if (receive.startsWith(define_var.uploadScore_NO)) // �ϴ������Ļظ�
		{
			message(define_var.uploadScoreHandle_NO, receive);
		} else if (receive.startsWith(define_var.info)) // ������������Ϣ
		{
			message(define_var.infoHandle, receive);
		} else if (receive.startsWith(define_var.newt_end)) // �����õģ�����ʱ��
		{
			message(define_var.newt_endHandle, receive);
		}
	}

	private void message(int i, String str) {
		Message msg = new Message();
		msg.what = i;
		msg.obj = str;
		mHandler.sendMessage(msg);
		if(mHandler!=null){
			Log.e("socketSingle","no null");
		}
		System.out.println("send data");
	}

	

	public void stopThread() {
		this.mConnectStop = true;
		System.out.println("stopThread");
		closeSocket();
		instance = null;

		
	}

	// �ر�socket
	public void closeSocket() {
		try{
			
			if (clientSocket != null) {
				System.out.println("closeSocket");
				clientSocket.close();
				clientSocket = null;
				Log.e("socekt","socket���ر���");
			}
			if (in != null) {
				MyUtils.close(in);
				in = null;
			}
			if(pw!=null){
				MyUtils.close(pw);
				pw=null;
			}
		}
		catch(Exception e){
			System.out.println("msg");
			e.printStackTrace();
		}
	}
	
//	//
//
//	// ���ַ���д�뵽�ı��ļ���
//	public void writeTxtToFile(String strcontent, String filePath,
//			String fileName) {
//		// �����ļ���֮���������ļ�����Ȼ�����
//		makeFilePath(filePath, fileName);
//
//		String strFilePath = filePath + fileName;
//		// ÿ��д��ʱ��������д
//		String strContent = strcontent + "\r\n";
//		try {
//			File file = new File(strFilePath);
//			if (!file.exists()) {
//				Log.d("TestFile", "Create the file:" + strFilePath);
//				file.getParentFile().mkdirs();
//				file.createNewFile();
//			}
//			RandomAccessFile raf = new RandomAccessFile(file, "rwd");
//			raf.seek(file.length());
//			raf.write(strContent.getBytes());
//			raf.close();
//		} catch (Exception e) {
//			Log.e("TestFile", "Error on write File:" + e);
//		}
//	}
//
//	// �����ļ�
//	public File makeFilePath(String filePath, String fileName) {
//		File file = null;
//		makeRootDirectory(filePath);
//		try {
//			file = new File(filePath + fileName);
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return file;
//	}
//
//	// �����ļ���
//	public static void makeRootDirectory(String filePath) {
//		File file = null;
//		try {
//			file = new File(filePath);
//			if (!file.exists()) {
//				file.mkdir();
//			}
//		} catch (Exception e) {
//			Log.i("error:", e + "");
//		}
//	}

}
