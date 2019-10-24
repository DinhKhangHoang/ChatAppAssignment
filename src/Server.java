import java.net.*;
import java.io.*;
//import myPackage.AccountJDBC;
import java.util.*;

//import Server.ClientThread;

import java.text.SimpleDateFormat;

// The Server
public class Server {
	//private static int uniqueID;
    private ArrayList<ClientThread> clientList; //Keep track of all clients
    private SimpleDateFormat theDate; //For displaying time and date
    private int port; //The port to listen on
    private boolean control; //Decides whether to keep going
    private Map<String, String> mapClient;
    
    Server(int portNumber){
    	this.port = portNumber;
    	theDate = new SimpleDateFormat("h:mma"); //Set the time
        clientList = new ArrayList<ClientThread>();
        mapClient = new HashMap<String, String>();
    }
    
    public void start() {
    	control = true;
        //Create the socket and wait for a client to request
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            
            //Loop forever to listen for client requests
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
    	Socket socket; // The listening/talk socket connection
        BufferedReader Input;
        PrintWriter Output;
        
        String username; //Username
        String date;
        String Address;
        int Port;
        int P2pPort;
        
        Boolean beingRequested = false; //A client is attempting to connect
        String requestResponse = null; //Client's response to request
        
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
                // read login packet
                while(true){
                    packet = Input.readLine();
                    String[] seperated = packet.split(" ");
                    if(seperated.length == 3 && seperated[0].equalsIgnoreCase("login")) {
                    	String value = mapClient.get(seperated[1]);
                    	if(value != null) {
                    		// neu tim thay client trong bang
                    		if(Integer.parseInt(value.split(":")[1]) > 0) {
                    			//client da dang nhap roi
                    			display("Username conflict detected.");
                                writeMsg("FALSE");
                                //writeMsg("[SERVER] Username " + username + " is already taken. Please enter another.");
                    		}
                    		else {
                    			//client chua dang nhap + sua port lai trong bang map
                    			mapClient.replace(seperated[1], Address + ":" + seperated[2]);
                    			this.P2pPort = Integer.parseInt(seperated[2]);
                    			this.username = seperated[1];
                    			break;
                    		}
                        }
                        else {
                        	// dang nhap lan dau them client vao bang
                        	mapClient.put(seperated[1], Address + ":" + seperated[2]);
                        	this.P2pPort = Integer.parseInt(seperated[2]);
                        	this.username = seperated[1];
                        	break;
                        }
                    }
                    else {
                    	//sai cu phap login
                    	display("Wrong request!");
                    	writeMsg("FALSE");
                    }
                }
                // dang nhap thanh cong
                writeMsg("TRUE");
                display(username + " just connected.");
            }
            catch (IOException e) {
                    display("Exception creating new Input/output Streams: " + e);
                    return;
            }
        	
        }
        
        public void run() {
        	boolean KeepGoing = true;
        	while(KeepGoing) {
        		try {
        			String packet = Input.readLine();
        			String[] seperated = packet.split(" ");
        			switch(seperated.length) {
        			case 1:
        				if (seperated[0].equalsIgnoreCase("LOGOUT")) {
        					this.P2pPort = 0;
        					//set port = 0 offline
        					mapClient.replace(this.username, "0");
        					close();
        					KeepGoing = false;
        					
        				}
        				else {
        					display("Wrong request!");
                        	writeMsg("FALSE");
        				}
        				break;
        			case 2:
        				if (seperated[0].equalsIgnoreCase("GET")) {
        					if(mapClient.get(seperated[1]) == null)
        						writeMsg("NOTFOUND");
        					else if(mapClient.get(seperated[1]) == "0")
        						writeMsg("Offline");
        					else {
        						//tim thay destination client
        						writeMsg("FOUND" + mapClient.get(seperated[1]));
        					}
        				}
        				else {
        					display("Wrong request!");
                        	writeMsg("FALSE");
        				}
        				break;
        			default:
        				display("Wrong request!");
        				writeMsg("FALSE");
        				break;
        			}
        		}
        		catch(IOException e){
        			display("Exception creating new Input/output Streams: " + e);
                    return;
        		}
        	}
        	remove(username);
        	close();
        }
        
        private void close() {
            // try to close the connection
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
        }
        
        // Write the string to the Client Output
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
