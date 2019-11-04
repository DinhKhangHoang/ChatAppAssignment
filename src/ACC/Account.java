package ACC;
import java.net.*;
import java.io.*;
import java.util.*;

public class Account {
	private String userName;
	private String ip_port;
	private String[] lstFriend;
	
	public Account(String usr, String lst) {
		this.userName = usr;
		this.ip_port = "0";
		if(lst.equals("")) {
			this.lstFriend = null;
		}
		else
			this.lstFriend = lst.split(",");
	}
	
	public Account(String usr, String ipport, String lst) {
		this.userName = usr;
		this.ip_port = ipport;
		if(lst.equals("")) {
			this.lstFriend = null;
		}
		else
			this.lstFriend = lst.split(",");
	}
	
	public void setUserName(String usr) {
		this.userName = usr;
	}
	
	public void setIpPort(String ipport) {
		this.ip_port = ipport;
	}
	
	public void setLstFriend(String lst) {
		this.lstFriend = lst.split(",");
	}
	
	public void addFriend(String name) {
		ArrayList<String> arrlst = new ArrayList<String>(Arrays.asList(this.lstFriend));
		arrlst.add(name);
		this.lstFriend = (String[])arrlst.toArray();
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public String getIpPort() {
		return this.ip_port;
	}
	
	public String[] getLstFriend() {
		return this.lstFriend;
	}
}
