package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts.Data;
import android.test.suitebuilder.TestSuiteBuilder.FailedToCreateTests;
import android.util.Log;

public class Client extends Thread {

	String message;
	String function;
	ObjectOutputStream obj;
	Context mcontext;
	String temp =null;
	public Uri uri;
	public String from_Port;
	public String to_port;
	public String key;
	public String value;
	public String message_key;
	public String message_value;
	public static String failedPort=null;
	ContentValues cv=new ContentValues();
	HashMap<String, String> map=new HashMap<String, String>();
	public Client(String message,String port,String function,Context context,Uri uri,String from_Port,String to_port)
	{
		this.message=message;
		this.function=function;
		this.mcontext=context;
		this.uri=uri;
		this.from_Port=from_Port;
		this.to_port=port;

		//	Message m=new Message();

	}
	public Client(HashMap<String, String> map,String message,String port,String function,Context context,Uri uri,String from_Port,String to_port)
	{
		this.message=message;
		this.function=function;
		this.mcontext=context;
		this.uri=uri;
		this.from_Port=from_Port;
		this.to_port=port;
		this.map=map;

		//	Message m=new Message();

	}
	public Client(String message,String key,String value,String port,String function,Context context,Uri uri,String from_Port,String to_port)
	{
		this.message=message;
		this.function=function;
		this.mcontext=context;
		this.uri=uri;
		this.from_Port=from_Port;
		this.to_port=to_port;
		this.key=key;
		this.value=value;

		//	Message m=new Message();

	}	

	public Client(Context context,Uri uri,String message_key,String message_value,String type)
	{
		this.mcontext=context;
		this.uri=uri;
		this.message_value=message_key;
		this.message_value=message_value;
		this.function=type;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		Message m=new Message();

		Socket socket=null;
		try {

			Log.v("port is",to_port);
			socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(to_port)*2);
			//socket.setSoTimeout(100);
			obj=new ObjectOutputStream(socket.getOutputStream());


			if(socket!=null){


				if(function.contains("$InsertKeySucc$"))
				{
					Log.v(message.split("\\|")[2]+"has send a client request to insert key value to",to_port);
					m.setKey("$InsertKeySucc$"+"|"+to_port);
					m.setValue(message);
					m.setType("$InsertKeySucc$");


				}


				else if(function.contains("$QuerySucc$"))
				{
					//Log.v("The Key sent from :"+from_Port+"is ",message.split("\\|")[0]);
					System.out.println("MEssage is"+message);
					Log.v("Key is",message.split("\\|")[0]);
					m.setKey(message.split("\\|")[0].trim());
					m.setToPort(to_port);
					m.setFrom_Port(from_Port);
					m.setType("$QuerySucc$");
					m.setMessage(message);


				}
				else if(function.contains("$QueryResponse$"))
				{
					m.setType("$QueryResponse$");
					m.setKey(key);
					Log.v("The Value for key"+key,"is "+value+"inside client");
					m.setValue(value);
					m.setToPort(to_port);


				}
				else if(function.contains("$Gdump$"))
				{
					Log.v("Inside Global dump","In client");
					Log.v("Message",message);
					m.setToPort(to_port);
					m.setType("$Gdump$");
					m.setValue(message);
				}
				else if(function.contains("$Recover$"))
				{

					m.setType("$Recover$");
					m.setMessage(message);
					m.setFrom_Port(from_Port);
					m.setToPort(to_port);
					
					

				}
				else if(function.contains("Delete"))
				{
					Log.v("message sent",message);
					m.setType("Delete");
					m.setFrom_Port(from_Port);
					m.setToPort(to_port);
					m.setMessage(message);

				}
				else if(function.contains("$Force_Recover$"))
				{
					Log.v("message sent",message);
					m.setType("$Force_Recover$");
					m.setFrom_Port(from_Port);
					m.setToPort(to_port);
					m.setMessage(message);

				}
					
				
				obj.reset();
				obj.writeObject(m);
				obj.flush();

				obj.close();
				socket.close(); 			    
			}

		} 	/*catch (SocketTimeoutException e){
        	SimpleDynamoProvider.isAlive=false;

		}*/catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			Log.v("Failed Port is",to_port);
		   SimpleDynamoProvider.count++;
			failedPort=to_port;
		
			if(m.getType()!=null)
			{
			if(m.getType().contains("$QuerySucc$")||m.getType().contains("$Gdump$")||m.getType().contains("Delete")||m.getType().contains("$Recover$"))
			{
				if(SimpleDynamoProvider.portIndexMap.get(failedPort)==4)
				{
					m.setToPort(SimpleDynamoProvider.getPortList.get(0));					

				}
				else
				{					

				m.setToPort(SimpleDynamoProvider.getPortList.get((SimpleDynamoProvider.portIndexMap.get(failedPort))+1));	

				}
				new Thread(new Client(message,m.getToPort(), m.getType(),mcontext,uri,m.getFrom_Port(),m.getToPort())).start();	
			}
			}
			
			if(m.getType().equals("$Recover$"))
			{
				SimpleDynamoProvider.query1Flag=true;
				SimpleDynamoProvider.InsertFlag=true;
			}
			e1.printStackTrace();
		}

		/*
		 * TODO: Fill in your client code that sends out a message.
		 */

	}



}
