package expection;

import java.util.ArrayList;
import java.util.HashMap;

import language.util.Tuple;

public class WrongElementChannel extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8770333969465630446L;
	String msg;
	HashMap<String, Integer> dependencies;
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public HashMap<String, Integer> getDependencies() {
		return dependencies;
	}
	public void setDependencies(HashMap<String, Integer> dependencies) {
		this.dependencies = dependencies;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public WrongElementChannel(String msg, HashMap<String, Integer> dependencies) {
		super();
		this.msg = msg;
		this.dependencies = dependencies;
	}
	
	
}