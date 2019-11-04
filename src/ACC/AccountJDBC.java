package ACC;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;


//import java.sql.Statement;
import java.sql.PreparedStatement;
public class AccountJDBC {
	private static String DB_URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=ChatApp;"
            + "integratedSecurity=true";
    private static String USER_NAME = "sa";
    private static String PASSWORD = "sa";
    //public String userName, passWord;
    private static AccountJDBC instance;
    public AccountJDBC(){
    	
    }
    
    public static AccountJDBC getInstance() {
    	if (instance == null) {
            instance = new AccountJDBC();
            
        }
        return instance;
    }
    public Connection getConnection(String dbURL, String userName, 
            String password) {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(dbURL, userName, password);
            //System.out.println("connect successfully!");
        } catch (Exception ex) {
            //System.out.println("connect failure!");
            ex.printStackTrace();
        }
        return conn;
    }
    public boolean InsertAccount(String userName, String lstFriend) {
    	try {
    		Connection conn = getConnection(DB_URL, USER_NAME, PASSWORD);
    		if (conn == null)
    			return false;
    		
    		
			PreparedStatement prstmt = conn.prepareStatement("insert into Accounts(USERNAME, LISTFRIEND) values (?,?)");
			prstmt.setString(1,  userName);
			prstmt.setString(2,  lstFriend);
			
			prstmt.executeUpdate();
			conn.close();
			return true;
    	}
    	catch(Exception ex){
    		
    	}
        
    	return true;
    }
    public boolean Login(String userName) {
    	boolean chch = false;
    	try {
    		Connection conn = getConnection(DB_URL, USER_NAME, PASSWORD);
    		if (conn == null)
    			return false;
    		PreparedStatement check = conn.prepareStatement("select * from Accounts where USERNAME = ?");
    		
    		check.setString(1, userName);
    		
    		
    		ResultSet result = check.executeQuery();
    		if (result.next() != false) {
    			//conn.close();
    			chch = true;
    		}
    		conn.close();
    		//return false;
    	}
    	catch(Exception ex){
    		
    	}
    	return chch;
    }
    
    public HashMap<String, Account> getMetadata() {
    	try {
    		Connection conn = getConnection(DB_URL, USER_NAME, PASSWORD);
    		if (conn == null)
    			return null;
    		PreparedStatement stmt = conn.prepareStatement("select * from Accounts");
    		
    		ResultSet rs = stmt.executeQuery();
    		HashMap<String, Account> mapClient = new HashMap<String, Account>();
    		while(rs.next()) {
	        	Account acc = new Account(rs.getString(1), rs.getString(2));
	        	mapClient.put(rs.getString(1), acc);
	        }
	        //rs.close();
    		conn.close();
    		//return false;
    		return mapClient;
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		return null;
    	}
    	
    }
    public boolean updateLstFriend(String username, String newfriend) {
    	try {
    		Connection conn = getConnection(DB_URL, USER_NAME, PASSWORD);
    		if(conn == null)
    			return false;
    		PreparedStatement stmt = conn.prepareStatement("select * from Accounts where USERNAME = ?");
    		stmt.setString(1, username);
    		ResultSet rs = stmt.executeQuery();
    		if(rs.next()) {
    			String newlst;
    			if(rs.getString(2).equals("")) {
    				newlst = newfriend;
    			}
    			else {
    				newlst = rs.getString(2) + "," + newfriend;
    			}
    			PreparedStatement update = conn.prepareStatement("update Accounts set LISTFRIEND=? where USERNAME = ?");
    			update.setString(1, newlst);
    			update.setString(2, username);
    			update.executeUpdate();
    		}
    		conn.close();
    		return true;
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		return false;
    	}
    }
    
    
}

