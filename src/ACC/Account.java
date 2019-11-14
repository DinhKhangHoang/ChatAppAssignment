package ACC;
import java.net.*;
import java.io.*;
import java.util.*;

public class Account {
	private String userName;
	private String ip_port;
	private String[] lstFriend;
	
	public Account(String usr, String lst) {
		setUserName(usr);
		setIpPort("0");
		setLstFriend(lst);
	}
	
	public Account(String usr, int ipport) {
		setUserName(usr);
		setIpPort(Integer.toString(ipport));
		setLstFriend("");
	}
	
	public Account(String usr, String ipport, String lst) {
		setUserName(usr);
		setIpPort(ipport);
		setLstFriend(lst);
	}
	
	public void setUserName(String usr) {
		this.userName = usr;
	}
	
	public void setIpPort(String ipport) {
		this.ip_port = ipport;
	}
	
	public void setLstFriend(String lst) {
		if(lst.equals("")) {
			this.lstFriend = null;
		}
		else
			this.lstFriend = lst.split(",");
	}
	
	public void addFriend(String name) {
		if(this.lstFriend != null) {
			ArrayList<String> arrlst = new ArrayList<String>(Arrays.asList(this.lstFriend));
			arrlst.add(name);
			this.lstFriend = arrlst.toArray(this.lstFriend);
		}
		else {
			this.lstFriend = new String[1];
			this.lstFriend[0] = name;
		}
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
	
	public boolean isFriend(String name) {
		if(this.lstFriend != null) {
			ArrayList<String> arrlst = new ArrayList<String>(Arrays.asList(this.lstFriend));
			if(arrlst.contains(name))
				return true;
			else
				return false;
		}
		else {
			return false;
		}
	}
}
