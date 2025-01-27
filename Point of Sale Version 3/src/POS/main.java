package POS;
/*import java.awt.Color;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;

public class login extends JFrame{
	public static void main (String [] arg){
		login newForm = new login();
		newForm.salesForm();
	}
	
	private login(){
		
	}
	
	private void salesForm(){
		new UI();
	}
}*/
import javax.swing.*;

import org.mindrot.jbcrypt.BCrypt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import com.datecs.fiscalprinter.tza.FMP10TZA;
import com.fazecast.jSerialComm.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class main extends JFrame{
/**
	 * 
	 */
private static final long serialVersionUID = 1L;
private JButton btnSubmit, btnSearchBarcode, btnSearchCode;
private JTextField txtUserName;
private JPasswordField txtPasswd;
private JLabel lblUserName;
private JLabel lblPasswd;
private JButton btnCancel, btnCreate;
private static main newForm;
private JMenu File, Product, PriceManagement, UserManagement;
private JMenuItem LogOut, NewProduct, EditProduct, PriceChange, BatchPriceChange, NewUser, EditUser, ResetPassword;
LocalDate mydate = LocalDate.now();
LocalDateTime dateTime = LocalDateTime.now();
DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String formatedDate = dateTime.format(ObjdateFormated);	
public static void main(String [] arg){
	newForm = new main();
	newForm.LoginForm();
}

public void LoginForm(){
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setBounds(200, 100, 600, 400);
	this.setLayout(null);
	this.setTitle("Login");
	this.getContentPane().setBackground(new Color(56,62,74));
	this.setForeground(Color.WHITE);
	lblUserName = new JLabel();
	lblUserName.setText("User Name:");
	lblUserName.setFont(new Font("Century Gothic", Font.PLAIN, 18));
	this.add(lblUserName);
	lblUserName.setBounds(50, 50, 100, 30);
	lblUserName.setForeground(Color.white);
	txtUserName = new JTextField();
	txtUserName.setFont(new Font("Century Gothic", Font.PLAIN, 18));
	this.add(txtUserName);
	txtUserName.setBounds(155, 50, 300, 30);
	lblPasswd = new JLabel();
	lblPasswd.setText("Password:");
	lblPasswd.setFont(new Font("Century Gothic", Font.PLAIN, 18));
	this.add(lblPasswd);
	lblPasswd.setBounds(50, 150, 100, 30);
	lblPasswd.setForeground(Color.white);
	txtPasswd = new JPasswordField();
	this.add(txtPasswd);
	txtPasswd.setBounds(155, 150, 300, 30);
	loginAction childLoginAction = new loginAction();
	Icon submitIcon = new ImageIcon("C:\\Libraries\\images\\key_go.png");
	//btnSubmit.setBorderPainted(false);
	btnSubmit = new JButton("Login", submitIcon);
	btnSubmit.setFont(new Font("Century Gothic", Font.BOLD, 18));
	btnSubmit.setBackground(new Color(51,245,12));
	btnSubmit.addActionListener(childLoginAction);
	btnSubmit.setForeground(Color.white);
	this.add(btnSubmit);
	btnSubmit.setBounds(240, 190, 105, 35);
	loginCheck checkNow = new loginCheck();
	btnSubmit.addKeyListener(checkNow);
	txtPasswd.addKeyListener(checkNow);
	txtUserName.addKeyListener(checkNow);
	btnCancel = new JButton("Cancel");
	btnCancel.setFont(new Font("Century Gothic", Font.BOLD, 18));
	btnCancel.setBackground(new Color(227,20,20));
	btnCancel.setForeground(Color.white);
	btnCancel.addActionListener(childLoginAction);
	this.add(btnCancel);
	btnCancel.setBounds(355, 190, 100, 35);
	this.setVisible(true);
}
private void performLogin(){
	// TODO Auto-generated method stub
		String uname = txtUserName.getText();
		String passwd = String.valueOf(txtPasswd.getPassword());
		//String SessionID = txtSessionId.
	//JOptionPane.showMessageDialog(null, uname + "  " + passwd);
		try {
            File file = new File("C:\\Libraries\\config.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String comPort = reader.readLine();
            String ip = reader.readLine();
            String user = reader.readLine();
            String pass = reader.readLine();
            String DBName = reader.readLine();
            reader.close();
            
            String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass;
            
            try (Connection con = DriverManager.getConnection(connectionUrl); 
                 PreparedStatement pstmt = con.prepareStatement("SELECT * FROM [aroniumdb].[dbo].[users] WHERE UserName = ?");) {
                pstmt.setString(1, uname);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(null, "!!!Wrong User Name or password!!!");
                } else {
                    String hashedPassword = rs.getString("Password");
                    System.out.println(hashedPassword);
                    
                    String compatibleHash = hashedPassword.replace("$2y$", "$2a$");
                    if (BCrypt.checkpw(passwd, compatibleHash)) {
                        String checkIfSessionExist = "SELECT * FROM [aroniumdb].[dbo].[user_sessions] WHERE Status = 'Open' and user_id = ?";
                        PreparedStatement pstmt1 = con.prepareStatement(checkIfSessionExist);
                        pstmt1.setInt(1, rs.getInt("Id"));
                        ResultSet rs1 = pstmt1.executeQuery();
                        
                        if (!rs1.next()) {
                            String insertSession = "INSERT INTO user_sessions (user_id, login_time) VALUES (?, ?)";
                            PreparedStatement pstmt2 = con.prepareStatement(insertSession);
                            pstmt2.setInt(1, rs.getInt("Id"));
                            //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            pstmt2.setString(2, mydate.toString());
                            pstmt2.executeUpdate();
                        }
                        
                        String sessionQuery = "SELECT session_id FROM [aroniumdb].[dbo].[user_sessions] WHERE user_id = ? AND Status = 'Open'";
                        PreparedStatement pstmt3 = con.prepareStatement(sessionQuery);
                        pstmt3.setInt(1, rs.getInt("Id"));
                        ResultSet rs2 = pstmt3.executeQuery();
                        
                        if (rs2.next()) {
                            String sessionId = rs2.getString("session_id");
                            new UI(uname, sessionId);
                            newForm.dispose();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "!!!Wrong User Name or password!!!");
                    }
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
		
}
private class loginCheck implements KeyListener{

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
			performLogin();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}


private class loginAction implements ActionListener {
    public void actionPerformed(ActionEvent arg0) {
        String uname = txtUserName.getText();
        String passwd = String.valueOf(txtPasswd.getPassword());
        
        if (arg0.getSource() == btnSubmit) {
            try {
                File file = new File("C:\\Libraries\\config.txt");
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String comPort = reader.readLine();
                String ip = reader.readLine();
                String user = reader.readLine();
                String pass = reader.readLine();
                String DBName = reader.readLine();
                reader.close();
                
                String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass;
                
                try (Connection con = DriverManager.getConnection(connectionUrl); 
                     PreparedStatement pstmt = con.prepareStatement("SELECT * FROM [aroniumdb].[dbo].[users] WHERE UserName = ?");) {
                    pstmt.setString(1, uname);
                    ResultSet rs = pstmt.executeQuery();
                    
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(null, "!!!Wrong User Name or password!!!");
                    } else {
                        String hashedPassword = rs.getString("Password");
                        System.out.println(hashedPassword);
                        
                        String compatibleHash = hashedPassword.replace("$2y$", "$2a$");
                        if (BCrypt.checkpw(passwd, compatibleHash)) {
                            String checkIfSessionExist = "SELECT * FROM [aroniumdb].[dbo].[user_sessions] WHERE Status = 'Open' and user_id = ?";
                            PreparedStatement pstmt1 = con.prepareStatement(checkIfSessionExist);
                            pstmt1.setInt(1, rs.getInt("Id"));
                            ResultSet rs1 = pstmt1.executeQuery();
                            
                            if (!rs1.next()) {
                                String insertSession = "INSERT INTO user_sessions (user_id, login_time) VALUES (?, ?)";
                                PreparedStatement pstmt2 = con.prepareStatement(insertSession);
                                pstmt2.setInt(1, rs.getInt("Id"));
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                pstmt2.setString(2, mydate.toString());
                                pstmt2.executeUpdate();
                            }
                            
                            String sessionQuery = "SELECT session_id FROM [aroniumdb].[dbo].[user_sessions] WHERE user_id = ? AND Status = 'Open'";
                            PreparedStatement pstmt3 = con.prepareStatement(sessionQuery);
                            pstmt3.setInt(1, rs.getInt("Id"));
                            ResultSet rs2 = pstmt3.executeQuery();
                            
                            if (rs2.next()) {
                                String sessionId = rs2.getString("session_id");
                                new UI(uname, sessionId);
                                newForm.dispose();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "!!!Wrong User Name or password!!!");
                        }
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
        if (arg0.getSource() == btnCancel) {
            txtUserName.setText("");
            txtPasswd.setText("");
            newForm.dispose();
        }
    }
}

}
