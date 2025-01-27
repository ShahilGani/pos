
/*import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

public class main {
	public static void main(String[] args) {

	        PrinterService printerService = new PrinterService();

	        System.out.println(printerService.getPrinters());

	        //print some stuff. Change the printer name to your thermal printer name.
	        printerService.printString("EPSON TM-T20II Receipt5", "Likuni phala Likuni phala Likuni phala \t\t\t500*2 \n\n");
	        printerService.printString("EPSON TM-T20II Receipt5", "testing testing 1 2 3eeeee \n\n");
	        printerService.printString("EPSON TM-T20II Receipt5", "testing testing 1 2 3eeeee \n\n");
	        printerService.printString("EPSON TM-T20II Receipt5", "testing testing 1 2 3eeeee \n\n\n\n\n\n\n\n");

	        // cut that paper!
	        byte[] cutP = new byte[] { 0x1d, 'V', 1 };

	        printerService.printBytes("EPSON TM-T20II Receipt5", cutP);
	}
}*/

import javax.swing.*;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;

public class main extends JFrame{
	private JButton btnSubmit, btnSearchBarcode, btnSearchCode;
	private JTextField txtUserName;
	private JPasswordField txtPasswd;
	private JLabel lblUserName;
	private JLabel lblPasswd;
	private JButton btnCancel, btnCreate;
	private static main newForm;
	private JMenu File, Product, PriceManagement, UserManagement;
	private JMenuItem LogOut, NewProduct, EditProduct, PriceChange, BatchPriceChange, NewUser, EditUser, ResetPassword;
		
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
						String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
						MessageDigest md = MessageDigest.getInstance("SHA-224");
						byte [] messageDigest = md.digest(passwd.getBytes());
						BigInteger BGpasswd = new BigInteger(1, messageDigest);
						String harshText = BGpasswd.toString();
						System.out.println(harshText);
				        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
				        	//String SQL = "SELECT * FROM [aroniumdb].[dbo].[product] where [aroniumdb].[dbo].[product].name like \'%" +  code + "%\'";
				        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[users] where UserName = \'" +  uname + "\' and Password = \'" + harshText + "\'";
				        	System.out.println(SQL);
				        	//JOptionPane.showMessageDialog(null, SQL);
				            ResultSet rs = stmt.executeQuery(SQL);
				            if (rs.next()==false){
				            	JOptionPane.showMessageDialog(null, "!!!Wrong User Name or password!!!");
				            }
				            else{
				            	//JOptionPane.showMessageDialog(null, "Login Successful");
				            	UI newUI = new UI();
				            	newForm.dispose();
				            }
				        }
				        // Handle any errors that may have occurred.
				        catch (SQLException e1) {
				            e1.printStackTrace();
				        }
					} catch (IOException | NoSuchAlgorithmException e1) {
						// TODO Auto-generated catch block
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
	private class loginAction implements ActionListener{
		
		public void actionPerformed(ActionEvent arg0) {
		 //TODO Auto-generated method stub
			String uname = txtUserName.getText();
			String passwd = String.valueOf(txtPasswd.getPassword());
			if (arg0.getSource()==btnSubmit){
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
						String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
						MessageDigest md = MessageDigest.getInstance("SHA-224");
						byte [] messageDigest = md.digest(passwd.getBytes());
						BigInteger BGpasswd = new BigInteger(1, messageDigest);
						String harshText = BGpasswd.toString();
						System.out.println(harshText);
				        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
				        	//String SQL = "SELECT * FROM [aroniumdb].[dbo].[product] where [aroniumdb].[dbo].[product].name like \'%" +  code + "%\'";
				        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[users] where UserName = \'" +  uname + "\' and Password = \'" + harshText + "\'";
				        	System.out.println(SQL);
				        	//JOptionPane.showMessageDialog(null, SQL);
				            ResultSet rs = stmt.executeQuery(SQL);
				            if (rs.next()==false){
				            	JOptionPane.showMessageDialog(null, "!!!Wrong User Name or password!!!");
				            }
				            else{
				            	//JOptionPane.showMessageDialog(null, "Login Successful");
				            	UI newUI = new UI();
				            	newForm.dispose();
				            }
				        }
				        // Handle any errors that may have occurred.
				        catch (SQLException e1) {
				            e1.printStackTrace();
				        }
					} catch (IOException | NoSuchAlgorithmException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				if (arg0.getSource()==btnCancel){
					txtUserName.setText("");
					txtPasswd.setText("");
					newForm.dispose();
				}
			}	
		}
}


 