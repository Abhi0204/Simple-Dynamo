package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.net.NetworkInfo.DetailedState;
import android.os.AsyncTask;
import android.provider.Contacts.Intents.Insert;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.SlidingDrawer;


public class SimpleDynamoProvider extends ContentProvider {

	static SQLiteDatabase sqLite_db;
	public static MyDbHelper dbHelper;
	public static String port_Str=null;
	public String base_port="5554";
	public static String ip_addr="10.0.2.2";
	public static String node_ID=null;
	public static int myPort=0;
	public static MyDbHelper Db;
	public static Uri mUri;
	ContentResolver myContentResolver;
	public static String startAvd="5554";
	public static String predecessor="";
	public static String successor="";
	Socket sock=null;
	public  static String db_key;
	public static String db_value;
	public static String sflag="";
	public static String fflag="";
	public static String myfflag="";
	public static boolean myflag;

	static final int SERVER_PORT = 10000;
	public static boolean flag=false;
	public static boolean flag1=false;
	public static ArrayList<String> arrList;
	public static String portCurrent;
	public static String successor1;
	public static String successor2;
	public static boolean Force_Insert=false;
	public static HashMap<String,Integer> portIndexMap;
	public static boolean querFlag=false;
	public static boolean isAlive=true;
	public static boolean checksucc1;
	public static boolean checksucc2;
	public static volatile Object myLock=new Object();
	public static String predecessor1;
	public static String predecessor2;
	public static ArrayList<String> list_pred_suc;
	ContentValues cv;
	SQLiteQueryBuilder queryBuilder;
	SQLiteDatabase query_db ;

	public static String succ1;
	public static String succ2;
	public static HashMap<String,ArrayList<String>> map_list;
	public static volatile int count=0;
	public static boolean InsertFlag=false;

	public static boolean query1Flag=false;
	public static ArrayList<String> getPortList=new ArrayList<String>();
	public static HashMap<String, String> myMap=new HashMap<String, String>();

	public static HashMap<String, String> MapSent=new HashMap<String, String>();
	public static HashMap<String, String> MapRecovered=new HashMap<String, String>();
	public static long past;
	public static long next;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub

		synchronized(myLock)
		{
			if(selection.equals("*"))
			{

				sqLite_db.delete(MyDbHelper.tableName, null, null);


				if(portIndexMap.get(port_Str)==4)
				{
					successor=getPortList.get(0);					

				}

				else
				{					

					successor=getPortList.get((portIndexMap.get(port_Str))+1);	

				}

				new Thread(new Client("*", successor, "Delete",getContext(),mUri,port_Str,successor)).start();	

			}
			else if(selection.equalsIgnoreCase("@"))
			{
				sqLite_db.delete(MyDbHelper.tableName,null,null );



			}
			else
			{

				int no=sqLite_db.delete(MyDbHelper.tableName,"key=?",new String[]{ selection});
				if(no==0)
				{	
					if(portIndexMap.get(port_Str)==4)
					{
						successor=getPortList.get(0);					

					}

					else
					{					

						successor=getPortList.get((portIndexMap.get(port_Str))+1);	

					}

					sqLite_db.delete(MyDbHelper.tableName,null,null );

					new Thread(new Client("@", successor, "Delete",getContext(),mUri,port_Str,successor)).start();	

				}


			}

			return 0;
		}
	}
	@Override

	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri  insert(Uri uri, ContentValues values)
	{

		// TODO Auto-generated method stub

		past=System.currentTimeMillis();
		synchronized(myLock)
		{
			try
			{	

				while(InsertFlag==false)
				{
					//if(System.currentTimeMillis()-past >5000)
					// InsertFlag=true;
				}

				Log.v("Just into Insert at",port_Str);
				db_key = (String) values.get(MyDbHelper.col_key);
				db_value = (String) values.get(MyDbHelper.col_value);
				Message m=new Message();
				m.setKey(db_key);
				m.setValue(db_value);

				for(int i=0;i<arrList.size();i++)
				{
					if(genHash(db_key).compareTo(arrList.get(i).trim())<=0)
					{
						if(i==3)
						{
							portCurrent=getPortList.get(i).trim();
							successor1= getPortList.get(4).trim();
							successor2= getPortList.get(0).trim();
							break;	
						}
						else if(i==4)
						{
							portCurrent=getPortList.get(4).trim();
							successor1= getPortList.get(0).trim();
							successor2= getPortList.get(1).trim();
							break;
						}

						else
						{
							portCurrent=getPortList.get(i).trim();
							successor1= getPortList.get(i+1).trim();
							successor2= getPortList.get(i+2).trim();
							break;
						}
					}

					else if(i==arrList.size()-1 &&(genHash(db_key).compareTo(arrList.get(i).split("\\|")[0].trim())>0))
					{
						portCurrent=getPortList.get(0).trim();
						successor1= getPortList.get(1).trim();
						successor2= getPortList.get(2).trim();
						break;
					}


				}
				if(portCurrent.equalsIgnoreCase(port_Str))
				{
					cv=new ContentValues();

					cv.put("key",db_key);
					cv.put("value",db_value);
					sqLite_db.insertWithOnConflict(MyDbHelper.tableName, null, cv,SQLiteDatabase.CONFLICT_REPLACE);

				}

				else
				{

					Thread Coordinator=new Client(m.getKey()+"|"+m.getValue()+"|"+port_Str,portCurrent,"$InsertKeySucc$",getContext(),mUri,port_Str,successor);

					Coordinator.start();
				}

				if(successor1.equals(port_Str))
				{				    

					cv=new ContentValues();


					cv.put("key",db_key);
					cv.put("value",db_value);
					sqLite_db.insertWithOnConflict(MyDbHelper.tableName, null, cv,SQLiteDatabase.CONFLICT_REPLACE);


				}
				else
				{

					Thread mysucc1=new Client(m.getKey()+"|"+m.getValue()+"|"+port_Str,successor1,"$InsertKeySucc$",getContext(),mUri,port_Str,successor1);

					mysucc1.start();
				}
				if(successor2.equalsIgnoreCase(port_Str))
				{				    

					cv=new ContentValues();

					cv.put("key",db_key);
					cv.put("value",db_value);
					sqLite_db.insertWithOnConflict(MyDbHelper.tableName, null, cv,SQLiteDatabase.CONFLICT_REPLACE);

				}
				else
				{


					Log.v("Port"+myPort+"has send a requets to insert key value to",successor2+" key Values are"+m.getKey()+"|"+m.getValue());

					Thread mysucc2= new Client(m.getKey()+"|"+m.getValue()+"|"+port_Str,successor2,"$InsertKeySucc$",getContext(),mUri,port_Str,successor2);
					mysucc2.start();
				}
			}	

			catch(Exception e)
			{
				e.printStackTrace();
			}



			return uri;
		}
	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub


		myLock=new Object();
		dbHelper = new MyDbHelper(getContext());
		dbHelper.getWritableDatabase();
		queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(MyDbHelper.tableName);
		query_db = dbHelper.getReadableDatabase();
		sqLite_db=dbHelper.getWritableDatabase();

		sqLite_db.delete(MyDbHelper.tableName,null,null );


		list_pred_suc=new ArrayList<String>();
		arrList=new ArrayList<String>();
		map_list=new HashMap<String, ArrayList<String>>();
		cv=new ContentValues();

		mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");

		TelephonyManager tel =(TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);

		port_Str = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		myPort=Integer.parseInt(port_Str)*2;


		arrList= new ArrayList<String>();
		try {
			arrList.add(genHash("5554")+"|"+"5554");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}
		try {
			arrList.add(genHash("5556")+"|"+"5556");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			arrList.add(genHash("5558")+"|"+"5558");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			arrList.add(genHash("5560")+"|"+"5560");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			arrList.add(genHash("5562")+"|"+"5562");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		Collections.sort(arrList);



		for(int i=0;i<arrList.size();i++)
		{
			getPortList.add(arrList.get(i).split("\\|")[1].trim());
		}


		portIndexMap=new HashMap<String, Integer>();
		portIndexMap.put(getPortList.get(0),0);
		portIndexMap.put(getPortList.get(1).trim(),1);
		portIndexMap.put(getPortList.get(2).trim(),2);
		portIndexMap.put(getPortList.get(3).trim(),3);
		portIndexMap.put(getPortList.get(4).trim(),4);


		Log.v("On Create AVD", port_Str);

		try
		{
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new Server().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		}
		catch (IOException e) {


		}


		return true;
	}

	@Override
	public Cursor  query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub

		past=System.currentTimeMillis();
		synchronized(myLock)
		{
			while(query1Flag==false)
			{
				//if(System.currentTimeMillis()-past>5000)
				//{
				//	query1Flag=true;
				//}
			}


			Log.v("Entered query at", port_Str+ " for selection"+selection);

			Message m=new Message();
			m.setKey(selection);

			Cursor cursor=null;

			if(selection.equalsIgnoreCase("@"))
			{


				Log.v("Queried for @ inside query function for",port_Str);

				cursor = queryBuilder.query(query_db,null,null, null, null, null, null);

				Log.v("query", selection);



				return cursor;
			}

			else if(selection.equalsIgnoreCase(("*")))

			{

				Log.v(" Just Inside * query function for port ",port_Str);
				cursor = queryBuilder.query(query_db,null,null, null, null, null, null);
				cursor.moveToFirst();
				String message=port_Str+"|";
				while (cursor.isAfterLast() == false) 
				{
					String key = cursor.getString(0);
					String value=cursor.getString(1);
					message=message+key+":"+value+"|";

					cursor.moveToNext();

				}

				if(portIndexMap.get(port_Str)==4)
				{
					successor=getPortList.get(0);					

				}
				else
				{					

					successor=getPortList.get((portIndexMap.get(port_Str))+1);	

				}
				new Thread(new Client(message, successor, "$Gdump$",getContext(),mUri,port_Str,successor)).start();	
				/*
				Log.v("Message has been passed from ",port_Str);

				Log.v("Busy Waiting Starts","for"+port_Str);
				 */

				while(flag==false)
				{

				}

				Log.v("Busy waiting ends for the global dump for port ",port_Str);
				MatrixCursor max=new MatrixCursor(new String[]{"key","value"});
				String[] strArray=fflag.split("\\|");
				for(String str:strArray)
				{			

					if(str.contains(":"))
					{	
						RowBuilder bg = max.newRow();
						bg.add("key",str.split("\\:")[0]);
						bg.add("value",str.split("\\:")[1]);

						Log.v("key",str.split("\\:")[0]);
						Log.v("value",str.split("\\:")[1]);
					}
				}
				fflag="";
				flag=false;
				return max;	
			}
			else

			{
				//Log.v("Querying at "+port_Str+ "for key",selection);
				cursor = queryBuilder.query(query_db,null,MyDbHelper.tableName +"."+MyDbHelper.col_key + "='"+selection+"'", null, null, null, null);

				if(!(cursor.getCount()>0))
				{
					//Log.v("Key Not Found at ",port_Str +"Key is"+ selection);
					if(portIndexMap.get(port_Str)==4)
					{
						successor=getPortList.get(0);					

					}
					else
					{
						successor=getPortList.get(portIndexMap.get(port_Str)+1);	
					}

					//	Log.v("successsor of  my port ->"+port_Str+" is",successor);
					//Log.v("Passing the Key="+selection+" to Successor by Port->"+port_Str+" To",successor);

					new Thread(new Client(selection+"|"+port_Str, successor, "$QuerySucc$",getContext(),mUri,port_Str,successor)).start();


					//Log.v("Busy Waiting starts at Particular Key+"+selection+" Check for Port"+port_Str,"Waiting Begins" );

					while(flag1==false)
					{
					}
					//	Log.v("Busy Waiting Ends at Particular Key"+selection+"Check for Port"+port_Str,"Waiting Ends" );


					MatrixCursor max=new MatrixCursor(new String[]{"key","value"});
					max.addRow(new String[]{selection,sflag});
					flag1=false;

					//Log.v("Key",sflag);
					sflag="";
					return max;	

				}
				else{
					Log.v("Key Found at port->"+port_Str+"Key is",selection);
					return cursor;
				}


			}
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	public class Server extends AsyncTask<ServerSocket, String , Void>{

		ServerSocket serverSocket;
		int server_port=10000;
		String var;
		String myavd;


		@Override
		protected Void doInBackground(ServerSocket... arg0) {
			// TODO Auto-generated method stub
			ObjectInputStream obj = null;
			ContentValues cv;


			ServerSocket serverSocket ;

			serverSocket = arg0[0];
			Socket socket = null;


			Thread rec=new Recovery();
			rec.start();	

			while(true)
			{	

				try {
					socket=serverSocket.accept();

					obj=new ObjectInputStream(socket.getInputStream());

					Message message = null;

					message = (Message)obj.readObject();


					obj.close();

					//Log.v("Enter the while loop at Server,for port",message.getKey().split("\\|")[1]);

					//Log.v(" Received message ", message.getKey() + " " + message.getValue());
					if(message.getType().contains("$InsertKeySucc$"))
					{
						/*
						Log.v("Insert Request has Come from "+ message.getValue().split("\\|")[2],"To insert Key value into "+message.getKey().split("\\|")[1]);
						Log.v("Inside Key Successeor in Server","For Free");
						Log.v("Inserting Key="+message.getValue().split("\\|")[0]+message.getValue().split("\\|"),"into "+message.getKey().split("\\|")[1]);
						Log.v("Inserting Value="+message.getValue().split("\\|")[1]+message.getValue().split("\\|"),"into "+message.getKey().split("\\|")[1]);
						 */
						String[] str=message.getValue().split("\\|");
						cv=new ContentValues();
						cv.put("key",str[0]);
						cv.put("value", str[1]);
						Log.v("My key:",str[0]);
						Log.v("My Value:",str[1]);

						sqLite_db.insertWithOnConflict(MyDbHelper.tableName, null, cv,SQLiteDatabase.CONFLICT_REPLACE);

						//new ForceInsert(getContext(),mUri,str[0],str[1]).start();




					}


					else if(message.getType().contains("$QuerySucc$"))
					{

						Cursor cursor=null;

						cursor = queryBuilder.query(query_db,null,MyDbHelper.tableName +"."+MyDbHelper.col_key + "='"+message.getKey().trim()+"'", null, null, null, null);
						String value = null;

						if(cursor.getCount()>0)
						{
							cursor.moveToFirst();
							while (cursor.isAfterLast() == false) 
							{
								value=cursor.getString(1);
								cursor.moveToNext();

							}
							/*	Log.v("Key"+message.getKey()+"Found value=",value);


							Log.v("successor of"+message.getFrom_Port()+"is ",message.getToPort());
							 */	String to_port=message.getMessage().split("\\|")[1].trim();
							 //Log.v("SEnd Kwy TO port",to_port);
							 Thread Client=new Thread(new Client(message.getMessage(),message.getKey(),value,to_port,"$QueryResponse$",getContext(),mUri,port_Str,to_port));
							 Client.start();
						}
						else 
						{
							if(portIndexMap.get(message.getToPort().trim())==4)
							{
								successor=getPortList.get(0);					

							}
							else

							{
								successor=getPortList.get(portIndexMap.get(message.getToPort())+1);	
							}

							//Log.v("Port"+port_Str+"Sending MEssage="+message.getValue()+"to",successor);
							Thread Client=new Thread(new Client(message.getMessage(),successor, "$QuerySucc$",getContext(),mUri,port_Str,successor));
							Client.start();
						}
					}			

					else if(message.getType().contains("$QueryResponse$"))
					{
						sflag=message.getValue().trim();
						flag1=true;
					}
					else if(message.getType().contains("$Gdump$"))
					{
						//Log.v("Inside global dump in server at port",message.getKey().split("\\|")[1]);
						//Log.v("portStr in global dump",port_Str);
						if(message.getValue().contains(port_Str))
						{
							//Log.v("MEssage has the port who initiytaed the rrequest wbich is",port_Str);
							fflag=message.getValue();
							flag=true;

						}
						else
						{
							Cursor cursor=null;

							cursor = queryBuilder.query(query_db,null,null, null, null, null, null);

							cursor.moveToFirst();
							String newMessage=message.getValue()+"|";
							while (cursor.isAfterLast() == false) 
							{
								String key = cursor.getString(0);
								String value=cursor.getString(1);
								newMessage=newMessage+key+":"+value+"|";

								cursor.moveToNext();
							}
							cursor.close();
							if(portIndexMap.get(message.getToPort())==4)
							{
								successor=getPortList.get(0);					
							}
							else

							{

								successor=getPortList.get(portIndexMap.get(message.getToPort())+1);	
							}
							//Log.v("Message has no port in it, forwarding the request to",successor);
							Thread Client=new Thread(new Client(newMessage,successor, "$Gdump$",getContext(),mUri,port_Str,successor));
							Client.start();

						}

					}


					else if(message.getType().contains("$Recover$"))
					{
						if(message.getMessage().contains(port_Str.trim()))
						{
							Log.v("Recovery Stops","YEs");
							myfflag=message.getMessage();
							myflag=true;

						}
						else
						{



							Cursor cursor =queryBuilder.query(query_db,null,null, null, null, null, null);
							cursor.moveToFirst();
							String newMessage=message.getMessage()+"|";
							while (cursor.isAfterLast() == false) 
							{
								String key = cursor.getString(0);
								String value=cursor.getString(1);
								newMessage=newMessage+key+":"+value+"|";

								cursor.moveToNext();
							}
							cursor.close();
							if(portIndexMap.get(message.getToPort().trim())==4)
							{
								successor=arrList.get(0).split("\\|")[1].trim();	


							}
							else
							{
								successor=arrList.get(portIndexMap.get(message.getToPort())+1).split("\\|")[1].trim();	
							}
							Thread client=new Thread(new Client(newMessage,successor.trim(), "$Recover$",getContext(),mUri,message.getToPort().trim(),successor.trim()));
							client.start();

						}
					}
					else if(message.getType().contains("$Force_Recover$"))
					{

						String[] msg=message.getMessage().split("\\|");
						for(String str:msg)
						{
							if(str.contains(":"))
							{
								String[] newMessage=str.split("\\:");
								String key=newMessage[0].trim();
								String value=newMessage[1].trim();
								for(int i=0;i<arrList.size();i++)
								{
									if(genHash(key).compareTo(arrList.get(i).trim())<=0)
									{
										if(i==3)
										{
											portCurrent=getPortList.get(i).trim();
											successor1= getPortList.get(4).trim();
											successor2= getPortList.get(0).trim();
											break;	
										}
										else if(i==4)
										{
											portCurrent=getPortList.get(4).trim();
											successor1= getPortList.get(0).trim();
											successor2= getPortList.get(1).trim();
											break;
										}

										else
										{
											portCurrent=getPortList.get(i).trim();
											successor1= getPortList.get(i+1).trim();
											successor2= getPortList.get(i+2).trim();
											break;
										}
									}

									else if(i==arrList.size()-1 &&(genHash(db_key).compareTo(arrList.get(i).split("\\|")[0].trim())>0))
									{
										portCurrent=getPortList.get(0).trim();
										successor1= getPortList.get(1).trim();
										successor2= getPortList.get(2).trim();
										break;
									}

									if(port_Str.equalsIgnoreCase(portCurrent)||port_Str.equalsIgnoreCase(successor1)||port_Str.equalsIgnoreCase(successor2))
									{
										ContentValues mycv=new ContentValues();
										mycv.put("key",key.trim());
										mycv.put("value", value.trim());
										System.out.println("Key"+key);
										System.out.println("Value"+value);

										Log.v("Key ",key.trim());
										Log.v("Value",value.trim());
										sqLite_db.insertWithOnConflict(MyDbHelper.tableName, null, mycv,SQLiteDatabase.CONFLICT_REPLACE);


									}
								}
							}




						}
					}
					else if(message.getType().contains("Delete"))
					{
						if(message.getMessage().contains("*"))
						{
							sqLite_db.delete(MyDbHelper.tableName,null,null);
							if(portIndexMap.get(message.getToPort())==4)
							{
								successor=getPortList.get(0);					
							}
							else
							{
								successor=getPortList.get(portIndexMap.get(message.getToPort())+1);	

							}
							Thread client=new Thread(new Client("*", successor, "Delete",getContext(),mUri,port_Str,successor));	
							client.start();


						}
						else if(message.getMessage().equalsIgnoreCase("@"))
						{
							sqLite_db.delete(MyDbHelper.tableName,null,null );

							if(portIndexMap.get(message.getToPort())==4)
							{
								successor=getPortList.get(0);					
							}
							else
							{
								successor=getPortList.get(portIndexMap.get(message.getToPort())+1);	
							}


							Thread Client=new Thread(new Client(message.getMessage(), successor, "Delete",getContext(),mUri,port_Str,successor));

						}
					}


				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}
		}
	}


	class Recovery extends Thread
	{
		String predecc1;
		String predecc2;
		String succ1;
		String succ2;
		Message m=new Message();
		String temp;
		HashMap<String, String> MapSent=new HashMap<String, String>();
		HashMap<String, String> MapRecovered=new HashMap<String, String>();
		public void run()
		{

			Log.v("Recover Being called by",port_Str);

			if(portIndexMap.get(port_Str.trim())==4)
			{
				successor=arrList.get(0).split("\\|")[1].trim();	
			}
			else
			{
				successor=arrList.get(portIndexMap.get(port_Str)+1).split("\\|")[1].trim();	
			}

			Log.v("My Port",port_Str);
			Log.v("Successoor",successor);

			Thread client=new Client(port_Str.trim(),successor.trim(),"$Recover$",getContext(),mUri,port_Str.trim(),successor.trim());
			client.start();


			Log.v("Busy Waiting Starts at Recover",port_Str);
			while(myflag==false);
			myflag=false;
			Log.v("Busy Waiting Ends at Recover",port_Str);

			ContentValues cv =new ContentValues();
			String[] message=myfflag.split("\\|");
			for(String str:message)
			{


				if(str.contains(":"))
				{
					String[] newMessage=str.split("\\:");

					MapSent.put(newMessage[0].trim(), newMessage[1].trim());
				}
			}


			//Got the original map to be stored in the Database
			MapRecovered=getRecovered(MapSent);
			for(Entry<String,String> etr : MapRecovered.entrySet())
			{
				String key = etr.getKey(); 
				String value = etr.getValue();  
				ContentValues mycv=new ContentValues();
				mycv.put("key",key.trim());
				mycv.put("value", value.trim());
				System.out.println("Key"+key);
				System.out.println("Value"+value);

				Log.v("Key ",key.trim());
				Log.v("Value",value.trim());
				sqLite_db.insertWithOnConflict(MyDbHelper.tableName, null, mycv,SQLiteDatabase.CONFLICT_REPLACE);


			}
			InsertFlag = true;
			query1Flag = true;
		}

		public  HashMap<String ,String> getRecovered(HashMap<String, String> map )
		{
			HashMap<String, String> GetRecoveredMap=new HashMap<String,String>();

			try
			{

				Iterator iterator=map.keySet().iterator();

				while(iterator.hasNext())
				{
					String key = iterator.next().toString();  
					String value = map.get(key.trim());  
					for(int i=0;i<arrList.size();i++)
					{
						if(genHash(key.trim()).compareTo(arrList.get(i).split("\\|")[0].trim())<=0)
						{
							if(i==3)
							{
								portCurrent=arrList.get(i).split("\\|")[1].trim();
								successor1= arrList.get(i+1).split("\\|")[1].trim();
								successor2= arrList.get(0).split("\\|")[1].trim();
								break;	
							}
							else if(i==4)
							{
								portCurrent=arrList.get(i).split("\\|")[1].trim();
								successor1= arrList.get(0).split("\\|")[1].trim();
								successor2= arrList.get(1).split("\\|")[1].trim();
								break;
							}

							else
							{
								portCurrent=arrList.get(i).split("\\|")[1].trim();
								successor1= arrList.get(i+1).split("\\|")[1].trim();
								successor2= arrList.get(i+2).split("\\|")[1].trim();
								break;
							}
						}

						else if(i==arrList.size()-1 &&(genHash(key.trim()).compareTo(arrList.get(i).split("\\|")[0].trim())>0))
						{
							portCurrent=arrList.get(0).split("\\|")[1].trim();
							successor1=arrList.get(1).split("\\|")[1].trim();
							successor2=arrList.get(2).split("\\|")[1].trim();
							break;
						}
					}

					if(port_Str.equalsIgnoreCase(portCurrent)||port_Str.equalsIgnoreCase(successor1)||port_Str.equalsIgnoreCase(successor2))
					{
						GetRecoveredMap.put(key.trim(), value.trim());
					}

				}
			}
			catch (NoSuchAlgorithmException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return GetRecoveredMap;

		}
	}
}