import java.net.*;
import java.io.*;
import ACC.AccountJDBC;
import ACC.Account;
import java.util.*;
import java.util.Date;
import java.sql.*;

//import Server.ClientThread;

import java.text.SimpleDateFormat;

// The Server
public class Server {
	//private static int uniqueID;
    private ArrayList<ClientThread> clientList; //Keep track of all clients
    private SimpleDateFormat theDate; //For displaying time and date
    private int port; //The port to listen on
    private boolean control; //Decides whether to keep going
    private Map<String, Account> mapClient;
    
    Server(int portNumber){
    	this.port = portNumber;
    	theDate = new SimpleDateFormat("h:mma"); //Set the time
        clientList = new ArrayList<ClientThread>();
        AccountJDBC db = AccountJDBC.getInstance();
        mapClient = db.getMetadata();//load data from database sql
    }
    
    public void start() {
    	control = true;
        //tao socket de lang nghe clients
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            
            //lap de nghe ket noi tu cac client
            while(control){
                display("Server is listening for Clients on port " + port + ".");
                Socket socket = serverSocket.accept(); //Pause and wait for connection
                //If listening has stopped
                if(!control) break;
                ClientThread newThread = new ClientThread(socket);
                clientList.add(newThread);
                newThread.start();
            }
            //Closing the server down due to break in loop
            try {
                serverSocket.close();
                for(int i = 0; i < clientList.size(); i++) {
                    ClientThread temp = clientList.get(i);
                    try {
                        temp.Input.close();
                        temp.Output.close();
                        temp.socket.close();
                    }
                    catch(IOException IOe){
                        //For safety
                    }
                }
            }
            catch(Exception e) {
                display ("Closing the Server and Clients: " + e);
            }
        }
        catch (IOException e) {
            String message = theDate.format(new Date()) + " Error on new ServerSocket: " + e + "\n";
            display(message);
        }
    }
    
    synchronized void remove(String usr) {
        for(int i = 0; i < clientList.size(); i++) {
            ClientThread temp = clientList.get(i);
            //It was found
            if(temp.username == usr) {
                clientList.remove(i);
                return;
            }
        }
    }
    synchronized boolean makeFriend(String cur, String des) {
    	for(int i = 0; i < clientList.size(); i++) {
            ClientThread temp = clientList.get(i);
            //It was found
            if(temp.username == des) {
                if(temp.writeMsg("REQUEST "+ cur))
                	return true;
                else
                	return false;
            }
        }
    	return false;
    }
    
    private void display(String prompt) {
        String time = theDate.format(new Date()) + " " + prompt;
            System.out.println(time);
    }
    
    public static void main(String[] args){
        int portNumber = 6000;
        Server server = new Server(portNumber);
        server.start();
    }
    
    class ClientThread extends Thread{
    	Socket socket; // 
        BufferedReader Input;
        PrintWriter Output;
        
        String username; //Username
        String date;
        String Address;
        int Port;
        int P2pPort;
        
        ClientThread(Socket socket){
        	this.socket =  socket;
        	System.out.println("Thread is attempting to create Data Streams.");
        	try {
                this.Output = new PrintWriter(socket.getOutputStream(),true);
                this.Input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Address = socket.getInetAddress().toString();
                Port = socket.getPort();
                this.date = new Date().toString();
                System.out.println("Address is: " + Address);
                System.out.println("Port is: " + Port);
                System.out.println("Date: " + date);
                String packet;
                boolean KeepGoing = true;
                // read login packet
                while(KeepGoing){
                    packet = Input.readLine();
                    String[] seperated = packet.split(" ");
                    if(seperated.length == 3 && seperated[0].equalsIgnoreCase("login")) {
                    	Account acc = mapClient.get(seperated[1]);
                    	if(acc != null) {
                    		// neu tim thay client trong bang
                    		
                    		if(!acc.getIpPort().equals("0")) {
                    			//client da dang nhap roi
                    			display("Username conflict detected.");
                                KeepGoing = writeMsg("FALSE");
                    		}
                    		else {
                    			//client co trong bang nhung chua dang nhap
                    			// cho phep dang nhap + sua port luu trong bang
                    			acc.setIpPort(Address + ":" + seperated[2]);
                    			mapClient.replace(seperated[1], acc);
                    			this.P2pPort = Integer.parseInt(seperated[2]);
                    			this.username = seperated[1];
                    			// dang nhap thanh cong
                                writeMsg("TRUE");
                                display(username + " just connected.");
                    			break;
                    		}
                        }
                        else {
                        	// dang ky tai khoan lan dau them client vao bang
                        	acc = new Account(seperated[1], Address + ":" + seperated[2], "");
                        	mapClient.put(seperated[1], acc);
                        	AccountJDBC db = AccountJDBC.getInstance();
                        	db.InsertAccount(seperated[1], ""); 	
                        	this.P2pPort = Integer.parseInt(seperated[2]);
                        	this.username = seperated[1];
                        	// dang nhap thanh cong
                            writeMsg("TRUE");
                            display(username + " just connected.");
                        	break;
                        }
                    }
                    else {
                    	//sai cu phap login
                    	display("Wrong request!");
                    	KeepGoing = writeMsg("FALSE");
                    }
                }
            }
            catch (IOException e) {
                    display("Exception creating new Input/output Streams: " + e);
                    return;
            }
        }
        
        //xu li tin hieu tu client
        public void run() {
        	if (!socket.isConnected()) {
        		remove(username);
        		close();
        		return;
        	}
        	boolean KeepGoing = true;
        	try {
        		while(KeepGoing) {
        		
        			String packet = Input.readLine();
        			String[] seperated = packet.split(" ");
        			switch(seperated.length) {
        			case 1:
        				if (seperated[0].equalsIgnoreCase("LOGOUT")) {
        					//logout
        					KeepGoing = false;
        				}
        				else if(seperated[0].equalsIgnoreCase("GET")) {
        					//gui list usr
        					KeepGoing = sendListFriend();
        				}
        				else {
        					display("Wrong request!");
                        	KeepGoing = writeMsg("FALSE");
        				}
        				break;
        			case 2:
        				if (seperated[0].equalsIgnoreCase("GET")) {
        					Account acc = mapClient.get(seperated[1]);
        					if(acc == null)
        						//khong tim thay
        						KeepGoing = writeMsg("NOTFOUND");
        					else if(acc.getIpPort() == "0")
        						//Offline
        						KeepGoing = writeMsg("Offline");
        					else {
        						//tim thay destination client
        						KeepGoing = writeMsg("FOUND " + acc.getUserName()+ " " + acc.getIpPort());
        					}
        				}
        				else if(seperated[0].equalsIgnoreCase("ADD")) {
        					Account acc = mapClient.get(seperated[1]);
        					if(acc == null)
        						KeepGoing = writeMsg("NOTEXIST");
        					else if(acc.getIpPort() == "0"){
        						KeepGoing = writeMsg("false");
        					}
        					else {
        						if(sendRequest(seperated[1])) {
        							writeMsg("false");
        						}
        						else {
        							writeMsg("true");
        						}
        					}
        				}
        				else if(seperated[0].equalsIgnoreCase("ACCEPTED")) {
        					Account acc1 = mapClient.get(username);
        					Account acc2 = mapClient.get(seperated[1]);
        					acc1.addFriend(seperated[1]);
        					acc2.addFriend(username);
        					mapClient.replace(username, acc1);
        					mapClient.replace(seperated[1], acc2);
        					AccountJDBC db = AccountJDBC.getInstance();
        					db.updateLstFriend(seperated[1], username);
        					db.updateLstFriend(username, seperated[1]);
        				}
        				else {
        					display("Wrong request!");
                        	KeepGoing = writeMsg("FALSE");
        				}
        				break;
        			default:
        				display("Wrong request!");
        				KeepGoing = writeMsg("FALSE");
        				break;
        			}
        		}
        		
        	}
        	catch(IOException e){
    			display("Exception creating new Input/output Streams: " + e);
                //return;
    		}
        	this.P2pPort = 0;
			//set port = 0 offline
        	Account acc = mapClient.get(this.username);
        	acc.setIpPort("0");
			mapClient.replace(this.username, acc);
        	remove(username);
        	close();
        }
        
        private boolean sendRequest(String desname) {
        	return makeFriend(username, desname);
        }
        //gui list user cho client
        private boolean sendListFriend() {
        	boolean successful = true;
        	successful = writeMsg("LIST");
        	Account acc = mapClient.get(username);
        	String[] lstFriend = acc.getLstFriend();
        	if(lstFriend != null) {
        		for(int i = 0; i < lstFriend.length; i ++) {
            		String ipport = mapClient.get(lstFriend[i]).getIpPort();
            		if(ipport.equals("0")) {
            			successful = writeMsg(lstFriend[i] + " " + "false");
            		}
            		else {
            			successful = writeMsg(lstFriend[i] + " " + "true");
            		}
            	}
        	}
        	
        	successful = writeMsg("END");
        	return successful;
        }
        
        // tat ket noi
        private void close() {
            
            try {
                    if(Output != null) Output.close();
            }
            catch(Exception e) {}
            try {
                    if(Input != null) Input.close();
            }
            catch(Exception e) {};
            try {
                    if(socket != null) socket.close();
            }
            catch (Exception e) {}
            //remove(username);
        }
        
        // gui tin nhan cho client
        private boolean writeMsg(String toSend) {
            if(!socket.isConnected()){
                //close();
                return false;
            }
            Output.println(toSend);
            return true;
        }
    }
}
