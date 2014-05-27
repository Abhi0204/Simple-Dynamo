package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {

	private String key;
	private String value;
	private  String type;	
	private String ToPort;
	private String from_Port;
	private String keyValue;
	private String message;
	private HashMap<String,String> mymap=new HashMap<String, String>();

	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getToPort() {
		return ToPort;
	}


	public void setToPort(String toPort) {
		ToPort = toPort;
	}


	public String getFrom_Port() {
		return from_Port;
	}


	public void setFrom_Port(String from_Port) {
		this.from_Port = from_Port;
	}


	public  String getType() {
		return type;
	}


	public  void setType(String type) {
		this.type = type;
	}


	public String getKeyValue() {
		return keyValue;
	}


	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public HashMap<String,String> getMymap() {
		return mymap;
	}


	public void setMymap(HashMap<String,String> mymap) {
		this.mymap = mymap;
	}
}


