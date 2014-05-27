package edu.buffalo.cse.cse486586.simpledynamo;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDbHelper extends SQLiteOpenHelper {

	public static final String tableName="grpmsg";
	public static final String DatabaseName="groupdatabase.db";
	public static final String col_key="key";
    public static final String col_value="value";
    public static final String port_check="portCheck";
    public static final String create_table = "CREATE TABLE " + tableName + " (" + col_key + " TEXT PRIMARY KEY, " + col_value	 + " TEXT NOT NULL"+ " );";
    public static final int db_Verison=1;

	
	public MyDbHelper(Context context, String name, CursorFactory factory,
			int version)
	{
		super(context,DatabaseName,factory,db_Verison);
	}

	public MyDbHelper(Context context) {
		this(context, DatabaseName, null, 1);

	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//Creating the database		
		db.execSQL(create_table);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		

        Log.w("mytag", (new StringBuilder("Upgrading from version ")).append(oldVersion).append(" to ").append(newVersion).append(", which will destroy all old data").toString());
        
	}
	


}
