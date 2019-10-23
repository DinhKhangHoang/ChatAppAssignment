import java.net.*;
import java.io.*;
import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
//import myPackage.AccountJDBC;

import java.text.SimpleDateFormat;

// The Server
public class Server {
	//private static int uniqueID;
    private ArrayList<ClientThread> clientList; //Keep track of all clients
    private SimpleDateFormat theDate; //For displaying time and date
    private int port; //The port to listen on
    private boolean control; //Decides whether to keep going
    private Map<String, Integer> mapClient;
    
    Server(int portNumber){
    	this.port = portNumber;
    	theDate = new SimpleDateFormat("h:mma"); //Set the time
        clientList = new ArrayList<ClientThread>();
        mapClient = new HashMap<String, Integer>();
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
                Output = new PrintWriter(socket.getOutputStream(),true);
                Input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Address = socket.getInetAddress().toString();
                Port = socket.getPort();
                System.out.println("Address is: " + Address);
                System.out.println("Port is: " + Port);
                
                String packet;
                // Read the username and check if username is duplicated
                while(true){
                    packet = Input.readLine();
                    String[] seperated = packet.split(" ");
                    
                    if(mapClient.get(seperated[0]) != null) {	
                        display("Username conflict detected.");
                        Output.println("false");
                        writeMsg("[SERVER] Username " + username + " is already taken. Please enter another.");
                    }
                    else {
                    	mapClient.put(seperated[0], Integer.parseInt(seperated[1]));
                    	break;
                    }
                    
                }
                Output.println("true");
                display(username + " just connected.");
            }
            catch (IOException e) {
                    display("Exception creating new Input/output Streams: " + e);
                    return;
            }
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
                close();
                return false;
            }
            Output.println(toSend);
            return true;
        }
    }
}
