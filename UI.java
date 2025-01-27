package POS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.datecs.fiscalprinter.tza.FMP10TZA;
import com.fazecast.jSerialComm.*;

import org.mindrot.jbcrypt.*;
import java.sql.*;

public class UI{
public class PaymentPopulator {
	    
	    // Method to get payment types from the database
	    public List<String> getPaymentTypesFromDB() {
	        List<String> paymentTypes = new ArrayList<>();
	        
	        // Database connection variables
	        String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=aroniumdb";
	        
	        String username = "sa";
	        String password = "password1@";

	        // SQL query to get payment names
	        String query = "SELECT Name FROM [dbo].[PaymentType] WHERE IsEnabled = 1";  // Filter only enabled payment types

	        // Establish database connection
	        try (Connection conn = DriverManager.getConnection(dbUrl, username, password);
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(query)) {
	             
	            // Process the result set and populate the paymentTypes list
	            while (rs.next()) {
	                String paymentName = rs.getString("Name");
	                paymentTypes.add(paymentName);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        
	        return paymentTypes;
	    }

	    // Method to populate the combo box with payment types
	    public void populatePaymentComboBox(JComboBox<String> cbPayment) {
	        List<String> paymentTypes = getPaymentTypesFromDB();  // Get payment types from DB
	        for (String paymentType : paymentTypes) {
	            cbPayment.addItem(paymentType);  // Add each payment name to the combo box
	        }
	    }
	    
	}
	JFrame frame = new JFrame();
    JButton btnSelect, btnSaveOrder, btnVoid, btnSearch, btnPrintRcpt, btnLastTrans, btnlogoff;
    DefaultTableModel model, resultModel;
    JTable tblSalesTable, tblSearchResult;
    JTextField txtSearch, txtBcode, txtTamount, txtChange, txtTotal, txtOrderNumber, txtTableNumber, txtDiscount;
    JLabel lblBcode, lbldepartment, lblTamount, lblChange, lblTotal, lblCustomer, lblSection, lblTableNumber, lblUsernameTxt, lblSessionIDTxt;
    JComboBox<String> cmbSection, cmbdepartment, cmbTableNumber, cbPayment;
    JPanel searchPanel;
    JDialog searchDialog;
    JScrollPane searchScrollPane;
    public static FMP10TZA mFMP;
    public static InputStream inputBuffer;
    public static OutputStream outputBuffer;
    public static SerialPort sp;
    static UI fp = null;
    double tendered = 0.0;
    double changing = 0.0;
    Font f = new Font("Arial", Font.BOLD, 32);
    Font sf = new Font("Arial", Font.PLAIN, 20);
    static String userId = "";
    static String sessionId = "";
    
    public static void main(String[] args) {
        new UI(userId, sessionId);
    }
    public String getUserName(){
		return lblUsernameTxt.getText();		
	}
    public UI(String Uname, String sessionId) {
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setTitle("Sales Flow General Store");
        
        Action act = new Action();
        change ch = new change();
        autoSearch pSearch = new autoSearch();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        //Font sf = new Font("SansSerif", Font.PLAIN, 14);
        //Font f = new Font("SansSerif", Font.PLAIN, 14);

        model = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        tblSalesTable = new JTable(model);
        tblSalesTable.setFont(sf);
        //tblSalesTable.setForeground(Color.WHITE);
        //tblSalesTable.setBackground(Color.BLACK);
        JScrollPane salesScroll = new JScrollPane(tblSalesTable);
        quantity addTotal = new quantity();
        tblSalesTable.addKeyListener(addTotal);
        model.addColumn("SKU");
        model.addColumn("Name");
        model.addColumn("Tax");
        model.addColumn("Qty");
        model.addColumn("Price");
        model.addColumn("Total Price");
        tblSalesTable.getColumn("SKU").setPreferredWidth(15);
		tblSalesTable.getColumn("Name").setPreferredWidth(550);
		tblSalesTable.getColumn("Tax").setPreferredWidth(1);
		tblSalesTable.getColumn("Qty").setPreferredWidth(2);
		tblSalesTable.getColumn("Price").setPreferredWidth(80);
		tblSalesTable.getColumn("Total Price").setPreferredWidth(80);
		tblSalesTable.setRowHeight(40);
        // Add table to the frame
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        frame.add(salesScroll, gbc);

        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        // Barcode or Product Code
        JLabel lblBcode = new JLabel("Barcode or Product Code");
        lblBcode.setFont(sf);
        lblBcode.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        frame.add(lblBcode, gbc);

        txtBcode = new JTextField();
        txtBcode.setFont(f);
        txtBcode.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        frame.add(txtBcode, gbc);

        // Tendered Amount
        JLabel lblTamount = new JLabel("Tendered Amount");
        lblTamount.setFont(sf);
        lblTamount.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        frame.add(lblTamount, gbc);

        txtTamount = new JTextField();
        txtTamount.setFont(f);
        txtTamount.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        frame.add(txtTamount, gbc);

        // Order Number
       /* JLabel lblCustomer = new JLabel("Order Number");
        lblCustomer.setFont(sf);
        lblCustomer.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 6;
        frame.add(lblCustomer, gbc);

        txtOrderNumber = new JTextField();
        txtOrderNumber.setFont(f);
        txtOrderNumber.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 1;
        frame.add(txtOrderNumber, gbc);*/

        // Total
        JLabel lblTotal = new JLabel("Total");
        lblTotal.setFont(sf);
        lblTotal.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 6;
        frame.add(lblTotal, gbc);

        txtTotal = new JTextField();
        txtTotal.setFont(f);
        txtTotal.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        frame.add(txtTotal, gbc);

        // Change
        JLabel lblChange = new JLabel("Change");
        lblChange.setFont(sf);
        lblChange.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 7;
        frame.add(lblChange, gbc);

        txtChange = new JTextField();
        txtChange.setFont(f);
        txtChange.setPreferredSize(new Dimension(400, 30));
        gbc.gridx = 1;
        frame.add(txtChange, gbc);


        // Adding buttons at the bottom
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 5, 5));
        //buttonPanel.setBackground(Color.BLACK);

        /*btnSaveOrder = new JButton("Save Order");
        btnSaveOrder.addActionListener(act);
        btnSaveOrder.setBackground(Color.YELLOW);
        btnSaveOrder.setForeground(Color.BLACK);
        btnSaveOrder.setFont(sf);
        btnSaveOrder.setMnemonic(KeyEvent.VK_F5);
        buttonPanel.add(btnSaveOrder);*/

        btnLastTrans = new JButton("Recall Last");
        btnLastTrans.addActionListener(act);
        btnLastTrans.setBackground(new Color(52, 235, 140));
        btnLastTrans.setForeground(Color.BLACK);
        btnLastTrans.setFont(sf);
        buttonPanel.add(btnLastTrans);

        btnVoid = new JButton("Delete");
        btnVoid.addActionListener(act);
        btnVoid.setFont(sf);
        btnVoid.setBackground(Color.RED);
        btnVoid.setForeground(Color.BLACK);
        buttonPanel.add(btnVoid);
        
        btnSearch = new JButton("Search");
        btnSearch.addActionListener(act);
        //receiptPrinter print = new receiptPrinter();
        btnSearch.setFont(sf);
        buttonPanel.add(btnSearch);

        btnPrintRcpt = new JButton("Print Receipt");
        btnPrintRcpt.addActionListener(act);
        receiptPrinter print = new receiptPrinter();
        btnPrintRcpt.addKeyListener(print);
        txtBcode.addKeyListener(act);
        txtBcode.addActionListener(act);
        txtTamount.addKeyListener(ch);
        btnPrintRcpt.setFont(sf);
        buttonPanel.add(btnPrintRcpt);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        frame.add(buttonPanel, gbc);
        
     // Adding bottom panel with session ID and username
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.setBackground(Color.BLACK);

        // Creating a sub-panel for the session ID
        JPanel sessionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sessionPanel.setBackground(Color.BLACK);

        JLabel lblSessionID = new JLabel("Session ID:");
        lblSessionID.setFont(sf);
        lblSessionID.setForeground(Color.WHITE);
        sessionPanel.add(lblSessionID);

        JLabel lblSessionIDTxt = new JLabel("");
        lblSessionIDTxt.setFont(sf);
        lblSessionIDTxt.setText(sessionId);
        lblSessionIDTxt.setForeground(Color.WHITE);
        sessionPanel.add(lblSessionIDTxt);

        bottomPanel.add(sessionPanel);

        // Creating a sub-panel for the username
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        usernamePanel.setBackground(Color.BLACK);

        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(sf);
        lblUsername.setForeground(Color.WHITE);
        usernamePanel.add(lblUsername);

        lblUsernameTxt = new JLabel("");
        lblUsernameTxt.setFont(sf);
        lblUsernameTxt.setText(Uname);
        lblUsernameTxt.setForeground(Color.WHITE);
        usernamePanel.add(lblUsernameTxt);

        bottomPanel.add(usernamePanel);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        frame.add(bottomPanel, gbc);
        
     // Create the right-side panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        rightPanel.setBackground(Color.BLACK);
        GridBagConstraints rightGbc = new GridBagConstraints();
        rightGbc.insets = new Insets(5, 5, 5, 5);
        rightGbc.fill = GridBagConstraints.HORIZONTAL;

     // Customer Name Label (inside right panel)
        JLabel lblCustomerName = new JLabel("Customer Name");
        lblCustomerName.setFont(sf);
        lblCustomerName.setForeground(Color.WHITE);
        rightGbc.gridx = 0;  // Align label to the left side
        rightGbc.gridy = 0;  // Position in the first row
        rightPanel.add(lblCustomerName, rightGbc);

        // Customer Name Text Field (inside right panel)
        JTextField txtCustomerName = new JTextField();
        txtCustomerName.setFont(f);
        txtCustomerName.setPreferredSize(new Dimension(400, 30));
        rightGbc.gridx = 1;  // Align text field to the right side of the label
        rightPanel.add(txtCustomerName, rightGbc);

        // Payment Label (inside right panel)
        JLabel lblPayment = new JLabel("Payment Mode");
        lblPayment.setFont(sf);
        lblPayment.setForeground(Color.WHITE);
        rightGbc.gridx = 0;  // Align label to the left side
        rightGbc.gridy = 1;  // Position below Customer Name
        rightPanel.add(lblPayment, rightGbc);
        
     // Create the Payment dropdown (ComboBox) and populate it
        cbPayment = new JComboBox<>();
        PaymentPopulator paymentPopulator = new PaymentPopulator();
        paymentPopulator.populatePaymentComboBox(cbPayment);  // Populate with payment types from DB
        cbPayment.setFont(sf);
        cbPayment.setPreferredSize(new Dimension(400, 30));
        rightGbc.gridx = 1;  // Position the dropdown next to the Payment label
        rightPanel.add(cbPayment, rightGbc);

        // Adding the Discount Button to the right panel
        btnlogoff = new JButton("Logoff");
        btnlogoff.setFont(sf);
        btnlogoff.addActionListener(act);
        rightGbc.gridx = 0; // Align the button to the left side
        rightGbc.gridy = 2;  // Position below the Payment field
        rightGbc.gridwidth = 1; // Span across both columns
        rightPanel.add(btnlogoff, rightGbc);
        btnlogoff.addActionListener(act);/*(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDiscountDialog();
            }
        });*/
        rightPanel.add(btnlogoff, rightGbc);

        // You can add more buttons or components to the right panel here

        // Add the right panel to the frame
        gbc.gridx = 2; // Adjust grid position for placing on the right
        gbc.gridy = 4; // Align with the barcode text field row
        gbc.gridheight = 4; // Span multiple rows if necessary
        gbc.fill = GridBagConstraints.BOTH;
        frame.add(rightPanel, gbc);

        frame.setVisible(true);
        btnPrintRcpt.setEnabled(false);
        //WinFocusListener winFocus = new WinFocusListener();
        //frame.addWindowListener(winFocus);
        
        createSearchDialog();
        //getsessionId();
	}
	
	/*public UI(String string) {
		// TODO Auto-generated constructor stub
		
	}*/
    
    /*private String getsessionId(){
    	String username = lblUsernameTxt.getText();
        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[user_sessions] WHERE user_id = (SELECT Id FROM [aroniumdb].[dbo].[Users] WHERE Username = \'" + username + "\') AND [aroniumdb].[dbo].[user_sessions].[Status] = \'Open\'";
        	//String SQL = "select * from panda.dbo.productlist where name like \'%" +  code + "%\'";
        	System.out.print(SQL);
            ResultSet rs = stmt.executeQuery(SQL);
            
            while(rs.next()){
            	sessionId = rs.getInt("session_id");
            	sessionId1 = sessionId.toString();
            }
            
        }
    	try {
			resultModel.setRowCount(0);
			File file = new File("C:\\libraries\\config.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String comPort = reader.readLine();
			String ip = reader.readLine();
			String user = reader.readLine();
			String pass = reader.readLine();
			String DBName = reader.readLine();
			reader.close();
			
			String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
			//JOptionPane.showMessageDialog(null, connectionUrl);
				//String username = lblUsernameTxt.getText();
		        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
		        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[user_sessions] WHERE user_id = (SELECT Id FROM [aroniumdb].[dbo].[Users] WHERE Username = \'" + username + "\') AND [aroniumdb].[dbo].[user_sessions].[Status] = \'Open\'";
		        	//String SQL = "select * from panda.dbo.productlist where name like \'%" +  code + "%\'";
		        	System.out.print(SQL);
		            ResultSet rs = stmt.executeQuery(SQL);
		            
		            while(rs.next()){
		            	Integer sessionId = rs.getInt("session_id");
		            	String sessionId1 = sessionId.toString();
		            }
		            
		        }
		        // Handle any errors that may have occurred.
		        catch (SQLException e1) {
		            e1.printStackTrace();
		        }
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		return sessionId1;
    }*/			
	private void showDiscountDialog() {
	    final JDialog discountDialog = new JDialog(frame, "Apply Discount", true);
	    discountDialog.setSize(400, 200);
	    discountDialog.setLayout(new GridBagLayout());
	    discountDialog.setLocationRelativeTo(frame); // Center the dialog
	    
	    GridBagConstraints gbc = new GridBagConstraints();
	    gbc.insets = new Insets(10, 10, 10, 10);
	    
	    JLabel lblDiscount = new JLabel("Enter Discount Percentage:");
	    lblDiscount.setFont(sf);
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    discountDialog.add(lblDiscount, gbc);
	    
	    final JTextField txtDiscount = new JTextField();
	    txtDiscount.setFont(f);
	    txtDiscount.setPreferredSize(new Dimension(200, 30));
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    txtDiscount.setFont(sf);
	    discountDialog.add(txtDiscount, gbc);
	    
	    JButton btnApplyDiscount = new JButton("Apply");
	    btnApplyDiscount.setFont(sf);
	    gbc.gridx = 1;
	    gbc.gridy = 2;
	    discountDialog.add(btnApplyDiscount, gbc);
	    
	    btnApplyDiscount.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            try {
	                double discountPercent = Double.parseDouble(txtDiscount.getText());
	                if (discountPercent < 0 || discountPercent > 100) {
	                    JOptionPane.showMessageDialog(discountDialog, "Please enter a percentage (0-100).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }
	                
	                applyDiscount(discountPercent);
	                discountDialog.dispose(); // Close the dialog
	            } catch (NumberFormatException ex) {
	                JOptionPane.showMessageDialog(discountDialog, "Please enter a valid number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
	            }
	        }
	    });
	    
	    discountDialog.setVisible(true);
	}
	public BigDecimal applyDiscount(BigDecimal amount, BigDecimal discountPercentage) {
	    BigDecimal discountAmount = amount.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
	    BigDecimal discountedAmount = amount.subtract(discountAmount);
	    return discountedAmount.setScale(2, RoundingMode.HALF_UP);
	}

	private void applyDiscount(double discountPercent) {
	    BigDecimal discountPercentage = BigDecimal.valueOf(discountPercent);
	    BigDecimal totalAfterDiscount = BigDecimal.ZERO;

	    for (int count = 0; count < model.getRowCount(); count++) {
	        // Get the original price and quantity
	        BigDecimal originalPrice = new BigDecimal(model.getValueAt(count, 4).toString());
	        BigDecimal quantity = new BigDecimal(model.getValueAt(count, 3).toString());

	        // Apply discount to the price
	        BigDecimal discountedPrice = applyDiscount(originalPrice, discountPercentage);

	        // Calculate the new subtotal after discount
	        BigDecimal discountedSubtotal = discountedPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);

	        // Update the table model with discounted values
	        model.setValueAt(discountedPrice, count, 4); // Update the price per item
	        model.setValueAt(discountedSubtotal, count, 5); // Update the subtotal

	        // Accumulate the total after discount
	        totalAfterDiscount = totalAfterDiscount.add(discountedSubtotal);
	    }

	    // Update the total in the text box
	    txtTotal.setText(totalAfterDiscount.toString());

	    // Assuming txtTamount holds the tendered amount
	    BigDecimal tenderedAmount = new BigDecimal(txtTamount.getText());
	    BigDecimal change = tenderedAmount.subtract(totalAfterDiscount).setScale(2, RoundingMode.HALF_UP);

	    // Update the change in the text box
	    txtChange.setText(change.toString());
	}
	
	private void createSearchDialog() {
	        searchDialog = new JDialog(frame, "Search Product", true);
	        searchDialog.setSize(800, 600);
	        searchDialog.setLayout(new BorderLayout());

	        JPanel searchPanel = new JPanel(new BorderLayout());
	        txtSearch = new JTextField();
	        txtSearch.setFont(sf);
	        txtSearch.getDocument().addDocumentListener(new autoSearch());
	        searchPanel.add(txtSearch, BorderLayout.NORTH);

	        resultModel = new DefaultTableModel();
	        resultModel.addColumn("SKU");
	        resultModel.addColumn("Name");
	        resultModel.addColumn("Price");
	        tblSearchResult = new JTable(resultModel);
	        tblSearchResult.setFont(sf);
	        searchScrollPane = new JScrollPane(tblSearchResult);
	        searchPanel.add(searchScrollPane, BorderLayout.CENTER);
	        
	        Action selectAct = new Action();
	        btnSelect = new JButton("Select");
	        btnSelect.setFont(sf);
	        btnSelect.addActionListener(selectAct);
	        searchPanel.add(btnSelect, BorderLayout.SOUTH);

	        searchDialog.add(searchPanel);
	 }
	
	/*public class searchDialog extends JDialog{
		private void showSearchPopup() {
	        // Create a new dialog for the search popup
	        final JDialog searchDialog = new JDialog(this, "Search Product", true);
	        searchDialog.setSize(400, 300);
	        searchDialog.setLocationRelativeTo(this);
	
	        JPanel searchPanel = new JPanel(new BorderLayout());
	        final JTextField searchField = new JTextField();
	        JButton searchBtn = new JButton("Search");
	
	        searchPanel.add(searchField, BorderLayout.CENTER);
	        searchPanel.add(searchBtn, BorderLayout.EAST);
	
	        final JTable searchResultsTable = new JTable(new DefaultTableModel(new Object[]{"Item", "Price"}, 0));
	        searchPanel.add(new JScrollPane(searchResultsTable), BorderLayout.SOUTH);
	
	        searchBtn.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String searchQuery = searchField.getText();
	                if (!searchQuery.isEmpty()) {
	                    searchProducts(searchQuery, (DefaultTableModel) searchResultsTable.getModel());
	                }
	            }
	
				private void searchProducts(String searchQuery,
						DefaultTableModel model) {
					// TODO Auto-generated method stub
					
				}
	        });
	
	        searchResultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                int row = searchResultsTable.getSelectedRow();
	                String item = searchResultsTable.getValueAt(row, 0).toString();
	                double price = Double.parseDouble(searchResultsTable.getValueAt(row, 1).toString());
	                DefaultTableModel orderTableModel = null;
					orderTableModel.addRow(new Object[]{item, 1, price});
	                searchDialog.dispose();
	            }
	        });
	
	        searchDialog.add(searchPanel);
	        searchDialog.setVisible(true);
	    }
	}*/
	private class autoSearch implements DocumentListener{

		@Override
		public void changedUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			check();
		}

		@Override
		public void insertUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			check();
		}

		@Override
		public void removeUpdate(DocumentEvent arg0) {
			// TODO Auto-generated method stub
			check();
		}
		private void check(){
			int stringLen = txtSearch.getText().length();
			if (stringLen>0){
				try {
				resultModel.setRowCount(0);
				File file = new File("C:\\libraries\\config.txt");
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String comPort = reader.readLine();
				String ip = reader.readLine();
				String user = reader.readLine();
				String pass = reader.readLine();
				String DBName = reader.readLine();
				reader.close();
								
				String code = txtSearch.getText();

				String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
				//JOptionPane.showMessageDialog(null, connectionUrl);
			        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[product] where [aroniumdb].[dbo].[product].name like \'%" +  code + "%\'";
			        	//String SQL = "select * from panda.dbo.productlist where name like \'%" +  code + "%\'";
			        	
			            ResultSet rs = stmt.executeQuery(SQL);
			            while(rs.next()){
			            	String SKU = rs.getString("id");
			            	String name = rs.getString("Name");
			            	String price = rs.getString("price");
			            	//int pric = Integer.parseInt(price);
			            	resultModel.addRow(new Object[] {SKU, name, price});
			            }
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else{
				resultModel.setRowCount(0);
			}
		}		
	}
	private class winFocusListener implements WindowListener{

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			txtBcode.grabFocus();
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	private class quantity implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			int row = tblSalesTable.getSelectedRow();
			int column = tblSalesTable.getSelectedColumn();
			String  quantity = (model.getValueAt(row, 3).toString());
			double stotal = (Double.parseDouble(model.getValueAt(row, 4).toString())) * (Double.parseDouble(quantity));
			BigDecimal QbdPrice = new BigDecimal(stotal);
        	QbdPrice = QbdPrice.setScale(2, RoundingMode.HALF_UP);
			model.setValueAt(QbdPrice, row, 5);
			
			double total = 0;
			double change = 0.0;
			for (int count = 0; count < model.getRowCount(); count++){
		           double price = Double.parseDouble(model.getValueAt(count, 3).toString());
		           double q = Double.parseDouble(model.getValueAt(count, 4).toString());
		           total = total + (price * q);		           
		    }
			BigDecimal sbdPrice = new BigDecimal(total);
			sbdPrice = sbdPrice.setScale(2, RoundingMode.HALF_UP);
			txtTotal.setText((sbdPrice).toString());
			btnPrintRcpt.setEnabled(true);
			change = change - total;
			BigDecimal bdchange = new BigDecimal(change);
			bdchange = bdchange.setScale(2, RoundingMode.HALF_UP);
			txtChange.setText((bdchange).toString());
			txtBcode.grabFocus();
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
	
	private class receiptPrinter implements KeyListener{
		private Config loadConfig(String path) throws IOException {
		    try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
		        Config config = new Config();
		        config.comPort = reader.readLine();
		        config.ip = reader.readLine();
		        config.user = reader.readLine();
		        config.pass = reader.readLine();
		        config.DBName = reader.readLine();
		        config.till = reader.readLine();
		        return config;
		    }
		}

		// Retrieve the next order number
		private int getNextOrderNumber(Connection con) throws SQLException {
		    String query = "SELECT TOP 1 OrderNumber FROM Document ORDER BY Id DESC"; // Adjust 'Id' to your PK or sorting column
		    try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
		        if (rs.next()) {
		            int lastOrderNumber = rs.getInt("OrderNumber");
		            System.out.println(lastOrderNumber);
		            int add = lastOrderNumber + 1;
		            System.out.println(add);
		            return lastOrderNumber + 1; // Increment the last order number by 1
		        }
		    }
		    // If no records are found, start with order number 1
		    return 1;
		}

		// Get the current session ID
		private String getSessionId(Connection con, String username) throws SQLException {
		    String query = "SELECT session_id FROM user_sessions WHERE user_id = (SELECT Id FROM Users WHERE Username = ?) AND Status = 'Open'";
		    try (PreparedStatement stmt = con.prepareStatement(query)) {
		        stmt.setString(1, username);
		        try (ResultSet rs = stmt.executeQuery()) {
		            if (rs.next()) {
		                return rs.getString("session_id");
		            }
		        }
		    }
		    throw new SQLException("Failed to retrieve session ID.");
		}

		// Insert transaction data
		private void insertTransactionData(Connection con, String orderNumber, String sessionId, Config config, String total)
		        throws SQLException {
		    String query = "INSERT INTO Document (Number, UserId, CustomerId, CashRegisterId, OrderNumber, Date, StockDate, Total,IsClockedOut, DocumentTypeId, WarehouseId, SessionId, DateCreated, DateUpdated) VALUES (?,(SELECT Id FROM Users WHERE Username = ?),1, ?, ?, GETDATE(), GETDATE(), ?, 0, 2, 1, ?, GETDATE(), GETDATE())";
		    System.out.println(query);
		    try (PreparedStatement stmt = con.prepareStatement(query)) {
		    	System.out.println(orderNumber);
		    	System.out.println(lblUsernameTxt.getText());
		    	System.out.println(config.till);
		        stmt.setInt(4, Integer.parseInt(orderNumber));
		        System.out.println(total);
		        System.out.println(sessionId);
		        stmt.setString(1, "19-200-" + orderNumber);
		        stmt.setString(2, lblUsernameTxt.getText());
		        stmt.setString(3, config.till);
		        stmt.setInt(4, Integer.parseInt(orderNumber));
		        stmt.setString(5, total);
		        stmt.setString(6, sessionId);
		        stmt.executeUpdate();
		    }
		}

		// Insert order items
		private void insertOrderItems(Connection con, String orderNumber) throws SQLException {
			

			System.out.println(orderNumber);
			String query = "INSERT INTO DocumentItem (DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
					           	 "((select TOP 1 id from aroniumdb.dbo.Document where OrderNumber =? ORDER BY Id DESC),?,?,?,?,?,?,?,?,?,?,?,?,?)";
					//INSERT INTO DocumentItem (DocumentId, ProductId, Tax, Quantity, Price, ExpectedQuantity, PriceBeforeTax, PriceAfterDiscount, Total) VALUES ((SELECT TOP 1 Id FROM Document WHERE OrderNumber = ?), ?, ?, ?, ?, ?, ?)";
		    try (PreparedStatement stmt = con.prepareStatement(query)) {
		        for (int i = 0; i < model.getRowCount(); i++) {
		        	
		        	String productId = model.getValueAt(i, 0).toString();
		            double qtySold = Double.parseDouble(model.getValueAt(i, 3).toString());

		            // Fetch the ProductCost from the Product table using ProductId
		            double productCost = getProductCostFromDatabase(con, productId);
		            
		            String description = model.getValueAt(i, 1).toString();
		            double price = Double.parseDouble(model.getValueAt(i, 4).toString());
		            double quantity = Double.parseDouble(model.getValueAt(i, 3).toString());

		            System.out.println("Order Item:");
		            System.out.println("Product ID: " + productId);
		            System.out.println("Description: " + description);
		            System.out.println("Price: " + price);
		            System.out.println("Quantity: " + quantity);
		            
		            stmt.setInt(1, Integer.parseInt(orderNumber));
		            stmt.setString(2, model.getValueAt(i, 0).toString());
		            stmt.setDouble(3, Double.parseDouble(model.getValueAt(i, 3).toString()));
		            stmt.setDouble(4, Double.parseDouble(model.getValueAt(i, 3).toString()));
		            stmt.setDouble(5, calculatePriceBeforeTax(i));
		            stmt.setInt(6, (0));
		            stmt.setInt(7,(0));
		            stmt.setDouble(8, Double.parseDouble(model.getValueAt(i, 4).toString()));
		            stmt.setDouble(9, productCost);
		            stmt.setDouble(10, Double.parseDouble(model.getValueAt(i, 4).toString()));
		            stmt.setDouble(11, Double.parseDouble(model.getValueAt(i, 5).toString()));
		            stmt.setDouble(12, Double.parseDouble(model.getValueAt(i, 4).toString()));
		            stmt.setDouble(13, Double.parseDouble(model.getValueAt(i, 5).toString()));
		            stmt.setInt(14, (0));
		            
		            stmt.addBatch();
		            System.out.println(query);
		        }
		        stmt.executeBatch();
		    }
		}

		// Update stock
		private void updateStock(Connection con) throws SQLException {
		    for (int i = 0; i < model.getRowCount(); i++) {
		        String productId = model.getValueAt(i, 0).toString();
		        double saleQty = Double.parseDouble(model.getValueAt(i, 3).toString());

		        String query = "SELECT Quantity FROM Stock WHERE ProductId = ?";
		        try (PreparedStatement stmt = con.prepareStatement(query)) {
		            stmt.setString(1, productId);
		            try (ResultSet rs = stmt.executeQuery()) {
		                if (rs.next()) {
		                    double newQuantity = rs.getDouble("Quantity") - saleQty;
		                    updateStockQuantity(con, productId, newQuantity);
		                } else {
		                    insertStockQuantity(con, productId, -saleQty);
		                }
		            }
		        }
		    }
		}

		private void updateStockQuantity(Connection con, String productId, double newQuantity) throws SQLException {
		    String query = "UPDATE Stock SET Quantity = ? WHERE ProductId = ?";
		    try (PreparedStatement stmt = con.prepareStatement(query)) {
		        stmt.setDouble(1, newQuantity);
		        stmt.setString(2, productId);
		        stmt.executeUpdate();
		    }
		}

		private void insertStockQuantity(Connection con, String productId, double quantity) throws SQLException {
		    String query = "INSERT INTO Stock (ProductId, Quantity) VALUES (?, ?)";
		    try (PreparedStatement stmt = con.prepareStatement(query)) {
		        stmt.setString(1, productId);
		        stmt.setDouble(2, quantity);
		        stmt.executeUpdate();
		    }
		}

		// Clear UI fields
		private void clearUI() {
		    model.setRowCount(0);
		    txtTotal.setText("");
		    txtTamount.setText("");
		    txtChange.setText("");
		    txtBcode.grabFocus();
		    btnPrintRcpt.setEnabled(false);
		}
		// Truncate long descriptions
		private String truncate(String text, int maxLength) {
		    return (text.length() > maxLength) ? text.substring(0, maxLength) : text;
		}
		// Utility to calculate price before tax
		private double calculatePriceBeforeTax(int rowIndex) {
		    double priceAfterTax = Double.parseDouble(model.getValueAt(rowIndex, 4).toString());
		    String taxCode = model.getValueAt(rowIndex, 2).toString();
		    return taxCode.equals("A") ? priceAfterTax / 1.165 : priceAfterTax;
		}
		
		private double getProductCostFromDatabase(Connection con, String productId) throws SQLException {
		    String query = "SELECT Cost FROM Product WHERE Id = ?";
		    try (PreparedStatement stmt = con.prepareStatement(query)) {
		        stmt.setString(1, productId);  // Set ProductId in the query
		        try (ResultSet rs = stmt.executeQuery()) {
		            if (rs.next()) {
		                return rs.getDouble("Cost");  // Return the ProductCost
		            } else {
		                throw new SQLException("Product not found for ProductId: " + productId);
		            }
		        }
		    }
		}

		// Then you can use the Map to fetch the cost for each product during the insert


		// Configuration holder class
		class Config {
		    String comPort, ip, user, pass, DBName, till;
		}
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getKeyCode()==KeyEvent.VK_F5){
				JOptionPane.showMessageDialog(null, "Test");
			}
			/*****if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
				try {
					File file = new File("C:\\libraries\\config.txt");
					@SuppressWarnings("resource")
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					String till = reader.readLine();
					String countOrders = "SELECT COUNT(dbo.Document.DocumentTypeId) as \"rows\" from Document where DocumentTypeId = 2";
					int numOrders = 0;
					String total = txtTotal.getText();
					
					String paddedOrders = "";
					LocalDate mydate = LocalDate.now();
					LocalDateTime dateTime = LocalDateTime.now();
					DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String formatedDate = dateTime.format(ObjdateFormated);
					//SimpleDateFormat simpleDate = new SimpleDateFormat(mydate.toString());
					String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
					try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmtSt = con2.createStatement();) {
			        	ResultSet rsSt = stmtSt.executeQuery(countOrders);
			        	while(rsSt.next()){
			        		numOrders = rsSt.getInt("rows");
			        		System.out.println(numOrders);
			        		numOrders = numOrders + 1;
			        		paddedOrders = String.format("%06d", numOrders);
			        		System.out.println(numOrders);
			        		System.out.println(mydate);
			        		System.out.println(formatedDate);
			        		System.out.println(paddedOrders);
			        	}
			        	//String userName ="";
			        	//userName = getUserName();
			        	String userName = lblUsernameTxt.getText();
			        	//System.out.println(lblSessionIDTxt.getText());
			        	//getsessionId();
			        	//String sessionID = lblSessionIDTxt.getText();
			        	System.out.println(lblUsernameTxt.getText());
			        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[user_sessions] WHERE user_id = (SELECT Id FROM [aroniumdb].[dbo].[Users] WHERE Username = \'" + userName + "\') AND [aroniumdb].[dbo].[user_sessions].[Status] = \'Open\'";
			        	//String SQL = "select * from panda.dbo.productlist where name like \'%" +  code + "%\'";
			        	System.out.print(SQL);
			            ResultSet rs = stmtSt.executeQuery(SQL);
			            String sessionId3 = "";
		            	Integer sessionId13 = 0;
			            while(rs.next()){
			            	sessionId13 = rs.getInt("session_id");
			            	sessionId3 = sessionId13.toString();
			            }
			        	String transactSQL = "insert into [aroniumdb].[dbo].[document] (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule, SessionId) values ('19-200-"+paddedOrders +"',(SELECT Id FROM Users WHERE Username = '" + userName + "'), '1', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0, '" + sessionId3 + "')";
			        	System.out.println(transactSQL);
			        	stmtSt.executeUpdate(transactSQL);
			        	for (int count = 0; count < model.getRowCount(); count++){
					           String id = (model.getValueAt(count, 0).toString());
					           double QtySold = Double.parseDouble(model.getValueAt(count, 3).toString());
					           double salesTax = 0.0;
					           double priceAfterTax = Double.parseDouble(model.getValueAt(count, 4).toString());
					           double itemTotalAfterTax = Double.parseDouble(model.getValueAt(count, 5).toString());
					           double priceBeforeTax = 0.0;
					           double totalPriceBeforeTax = 0.0;
					           String tax = (model.getValueAt(count, 2).toString());
					           if (tax.equals("A")){
					        	   salesTax = priceAfterTax * 0.165;
					        	   priceBeforeTax = priceAfterTax - salesTax;
					        	   itemTotalAfterTax = (itemTotalAfterTax - (salesTax * QtySold));
					           }
					           else{
					        	   priceBeforeTax = priceAfterTax;
					        	   itemTotalAfterTax = itemTotalAfterTax;
					           }
					           String upDateDocItem = "insert into [aroniumdb].[dbo].[DocumentItem](DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
					           	 "((select id from aroniumdb.dbo.Document where OrderNumber ='" + numOrders + "')," + id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0)";
					           System.out.println(upDateDocItem);
					           stmtSt.executeUpdate(upDateDocItem);
			        	}	            
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
					for (int count = 0; count < model.getRowCount(); count++){
				           String id = (model.getValueAt(count, 0).toString());
				           double sale = Double.parseDouble(model.getValueAt(count, 3).toString());
				           String SQL = "SELECT * FROM [aroniumdb].[dbo].[stock] where productId =" +  id;
						   System.out.println(SQL);
				           try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
					        	//String SQL = "SELECT * FROM [aroniumdb].[dbo].[stock] where Id = " +  id ;
					        	ResultSet rs = stmt.executeQuery(SQL);
					        	if (rs.next()==false){
						          float stock = -1; //rs.getFloat("Quantity");
						          System.out.println(stock);
						          //JOptionPane.showMessageDialog(null, Double.toString(stock));
						          double balance = stock; //- sale;
							          try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmt2 = con2.createStatement();) {
									    	String updateSQL = "insert into [aroniumdb].[dbo].[stock] (productID, warehouseId, Quantity) values (" +  id + "," + 1 +", " + balance + ")" ;
									       	System.out.println(updateSQL);
									    	stmt2.executeUpdate(updateSQL);  
									  }
									  // Handle any errors that may have occurred.
									  catch (SQLException e1) {
									      e1.printStackTrace();
									  }
						         }else{
						        	 System.out.println(sale);
					        		//while(rs.next()){
						            	float stock = rs.getFloat("Quantity");
						            	System.out.println(stock);
						            	
						            	//JOptionPane.showMessageDialog(null, Double.toString(stock));
						            	double balance = stock - sale;
						            	try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmt2 = con2.createStatement();) {
								        	String updateSQL = "update [aroniumdb].[dbo].[stock] set Quantity = " + balance + "where ProductId =" +  id;
								        	stmt2.executeUpdate(updateSQL);
								            
								        }
								        // Handle any errors that may have occurred.
								        catch (SQLException e1) {
								            e1.printStackTrace();
								        }
						            	
						            //}
					        	}
					        }
					        // Handle any errors that may have occurred.
					        catch (SQLException e1) {
					            e1.printStackTrace();
					        }
				           
				    }
					//Connect(comPort);
					/*Connect(comPort);
					String Tandered = txtTamount.getText();
					String Pmode = "P";
					mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
					openFiscalRecipt0("1", "0000", "1");*/
					/**sp = SerialPort.getCommPort(comPort);
					
					sp.setComPortParameters(115200, 8, 1, 0);
					sp.setFlowControl(0);
					sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
					
					inputBuffer = sp.getInputStream();
		            Thread.sleep(2000);
		            outputBuffer = sp.getOutputStream();
		        
		            Thread.sleep(800);
		            (new Thread(new SerialWriter(outputBuffer))).start();
		            Thread.sleep(800);
		            (new Thread(new SerialReader(inputBuffer))).start();
		            
		            Thread.sleep(2000);
		            System.out.println("Connected");
		            String Tandered = txtTamount.getText();
					String Pmode = "P";
					mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
					openFiscalRecipt0("1", "0000", "1");
					
					int strSize = 0;
					
					for (int count = 0; count < model.getRowCount(); count++){
						
				           String desc = ((model.getValueAt(count, 1).toString()));
				           strSize = desc.length();
				           if (strSize>25) {
				        	   desc = desc.substring(0, 25);
				           }
				           String tax = (model.getValueAt(count, 2).toString());
				           String price = (model.getValueAt(count, 4).toString());
				           String q = (model.getValueAt(count, 3).toString());
				           sellItem(desc, tax, price, q);
						   
				    }
					/*calcSumTotalPaidAmnt(Pmode, Tandered);
					totalCash();
					closingFiscalReceipt();
					disconnect();*/
					/**model.setRowCount(0);
					txtTotal.setText("");
					txtChange.setText("");
					txtTamount.setText("");
					txtBcode.grabFocus();
					btnPrintRcpt.setEnabled(false);
					//Connect(comPort);
					System.out.println("Connect COMPort: " + comPort);
					 
				 	sp = SerialPort.getCommPort(comPort);
					
					sp.setComPortParameters(115200, 8, 1, 0);
					sp.setFlowControl(0);
					sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
					sp.openPort();		
					if(sp.isOpen()==true)
					{
						System.out.println("connection success");			             
			            inputBuffer = sp.getInputStream();
			            Thread.sleep(800);
			            outputBuffer = sp.getOutputStream();
			            System.out.println("stream success");	
			            Thread.sleep(800);
			            (new Thread(new SerialWriter(outputBuffer))).start();
			            Thread.sleep(800);
			            (new Thread(new SerialReader(inputBuffer))).start();
			            
			            Thread.sleep(800);
			            System.out.println("Connected");
			            String Tandered = txtTamount.getText();
						String Pmode = "P";
						mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());
						
						openFiscalRecipt0("1", "0000", "1");
						String transactionNo = "Receipt No: 19-200-"+paddedOrders;
						//printNonFiscalText(transactionNo);
						printFreeFiscalText(transactionNo);
						//String customer = txtCustomer.getText();
						printFreeFiscalText("Customer Name");
						//printFreeFiscalText(customer);
						printFreeFiscalText("-----------------------------------------------------------------");
						int strSize = 0;
						for (int count = 0; count < model.getRowCount(); count++){
							
					           String desc = ((model.getValueAt(count, 1).toString()));
					           strSize = desc.length();
					           if (strSize>25) {
					        	   desc = desc.substring(0, 25);
					           }
					           String tax = (model.getValueAt(count, 2).toString());
					           String price = (model.getValueAt(count, 4).toString());
					           String q = (model.getValueAt(count, 3).toString());
					           sellItem(desc, tax, price, q);
							   
					    }
						calcSumTotalPaidAmnt(Pmode, Tandered);
						totalCash();
						closingFiscalReceipt();
						disconnect();
						model.setRowCount(0);
						txtTotal.setText("");
						txtChange.setText("");
						txtTamount.setText("");
						//txtCustomer.setText("");
						txtBcode.grabFocus();
						btnPrintRcpt.setEnabled(false);
					}
					else
					{
						sp.closePort();
						System.out.println("Failed...");
						//JOptionPane.showMessageDialog(null, "Failed to connect to printer");
						System.out.println("Connect COMPort: " + comPort);
						 
					 	sp = SerialPort.getCommPort(comPort);
						
						sp.setComPortParameters(115200, 8, 1, 0);
						sp.setFlowControl(0);
						sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
								
						try{
							sp.closePort();
							sp.openPort();
							if(sp.openPort()==true)
							{			             
					            inputBuffer = sp.getInputStream();
					            Thread.sleep(800);
					            outputBuffer = sp.getOutputStream();
					        
					            Thread.sleep(800);
					            (new Thread(new SerialWriter(outputBuffer))).start();
					            Thread.sleep(800);
					            (new Thread(new SerialReader(inputBuffer))).start();
					            
					            Thread.sleep(800);
					            System.out.println("Connected");
					            String Tandered = txtTamount.getText();
								String Pmode = "P";
								mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
								openFiscalRecipt0("1", "0000", "1");
								String transactionNo = "Receipt No. 19-200-"+paddedOrders;
								//printNonFiscalText(transactionNo);
								printFreeFiscalText(transactionNo);
								printFreeFiscalText("-----------------------------------------------------------------");
								int strSize = 0;
								for (int count = 0; count < model.getRowCount(); count++){
									
							           String desc = ((model.getValueAt(count, 1).toString()));
							           strSize = desc.length();
							           if (strSize>25) {
							        	   desc = desc.substring(0, 25);
							           }
							           String tax = (model.getValueAt(count, 2).toString());
							           String price = (model.getValueAt(count, 4).toString());
							           String q = (model.getValueAt(count, 3).toString());
							           sellItem(desc, tax, price, q);
									   
							    }
								calcSumTotalPaidAmnt(Pmode, Tandered);
								totalCash();
								closingFiscalReceipt();
								disconnect();
								model.setRowCount(0);
								txtTotal.setText("");
								txtChange.setText("");
								txtTamount.setText("");
								txtBcode.grabFocus();
								btnPrintRcpt.setEnabled(false);
							}
							else
							{
								sp.closePort();
								System.out.println("Failed...");
								JOptionPane.showMessageDialog(null, "Failed to connect to printer");
							}
							
						}
						catch(SerialPortTimeoutException e){
							e.printStackTrace();
						}
					/*sp = SerialPort.getCommPort(comPort);
					
					sp.setComPortParameters(115200, 8, 1, 0);
					sp.setFlowControl(0);
					sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
							
					if(sp.openPort()==true)
					{
									             
			            inputBuffer = sp.getInputStream();
			            Thread.sleep(2000);
			            outputBuffer = sp.getOutputStream();
			        
			            Thread.sleep(800);
			            (new Thread(new SerialWriter(outputBuffer))).start();
			            Thread.sleep(800);
			            (new Thread(new SerialReader(inputBuffer))).start();
			            
			            Thread.sleep(2000);
			            System.out.println("Connected");
			            String Tandered = txtTamount.getText();
						String Pmode = "P";
						mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
						openFiscalRecipt0("1", "0000", "1");	
						//String customer = txtCustomer.getText();
						//printFreeFiscalText("Cusoumer Name: " + customer);
						//int strSize = 0;
						for (int count = 0; count < model.getRowCount(); count++){
							
					           String desc = ((model.getValueAt(count, 1).toString()));
					           strSize = desc.length();
					           if (strSize>25) {
					        	   desc = desc.substring(0, 25);
					           }
					           String tax = (model.getValueAt(count, 2).toString());
					           String price = (model.getValueAt(count, 4).toString());
					           String q = (model.getValueAt(count, 3).toString());
					           sellItem(desc, tax, price, q);
							   
					    }
						calcSumTotalPaidAmnt(Pmode, Tandered);
						totalCash();
						closingFiscalReceipt();
						disconnect();
						model.setRowCount(0);
						txtTotal.setText("");
						txtChange.setText("");
						txtTamount.setText("");
						//txtCustomer.setText("");
						txtBcode.grabFocus();
						btnPrintRcpt.setEnabled(false);
					}
					else
					{
						sp.closePort();
						System.out.println("Failed...");
						JOptionPane.showMessageDialog(null, "Failed to connect to printer");
						sp = SerialPort.getCommPort(comPort);
						
						sp.setComPortParameters(115200, 8, 1, 0);
						sp.setFlowControl(0);
						sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
								
						if(sp.openPort()==true)
						{
										             
				            inputBuffer = sp.getInputStream();
				            Thread.sleep(1500);
				            outputBuffer = sp.getOutputStream();
				        
				            Thread.sleep(1500);
				            (new Thread(new SerialWriter(outputBuffer))).start();
				            Thread.sleep(1500);
				            (new Thread(new SerialReader(inputBuffer))).start();
				            
				            Thread.sleep(800);
				            System.out.println("Connected");
				            String Tandered = txtTamount.getText();
							String Pmode = "P";
							mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
							openFiscalRecipt0("1", "0000", "1");					
							int strSize = 0;
							for (int count = 0; count < model.getRowCount(); count++){
								
						           String desc = ((model.getValueAt(count, 1).toString()));
						           strSize = desc.length();
						           if (strSize>25) {
						        	   desc = desc.substring(0, 25);
						           }
						           String tax = (model.getValueAt(count, 2).toString());
						           String price = (model.getValueAt(count, 4).toString());
						           String q = (model.getValueAt(count, 3).toString());
						           sellItem(desc, tax, price, q);
								   
						    }
							calcSumTotalPaidAmnt(Pmode, Tandered);
							totalCash();
							closingFiscalReceipt();
							disconnect();
							model.setRowCount(0);
							txtTotal.setText("");
							txtChange.setText("");
							txtTamount.setText("");
							txtBcode.grabFocus();
							btnPrintRcpt.setEnabled(false);
						}
						else
						{
							sp.closePort();
							System.out.println("Failed...");
							JOptionPane.showMessageDialog(null, "Failed to connect to printer");
						}
					}
					
					//model.addRow(new Object[] {"Description", "TAX ID", "Quantity", "Price"});
								
				} }catch (IllegalArgumentException | IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					JOptionPane.showMessageDialog(null,e2);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,e1);
				} catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,e1);
				}				
			}**/
			if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
				try {
		            // Load configuration
		            final Config config = loadConfig("C:\\libraries\\config.txt");

		            // Establish Database Connection
		            String connectionUrl = String.format("jdbc:sqlserver://%s:1433;databaseName=%s;user=%s;password=%s",
		                    config.ip, config.DBName, config.user, config.pass);

		            // Start Transaction
		            System.out.println("Starting transaction...");
		            try (Connection con = DriverManager.getConnection(connectionUrl)) {
		                con.setAutoCommit(false);
		                System.out.println("Database connection established.");

		                // Get order details
		                int orderNumber = getNextOrderNumber(con);
		                final String formattedOrderNumber = String.format("%06d", orderNumber);
		                System.out.println("Order number retrieved: " + orderNumber);

		                // Insert transaction data
		                String sessionId = getSessionId(con, lblUsernameTxt.getText());
		                insertTransactionData(con, formattedOrderNumber, sessionId, config, txtTotal.getText());

		                // Insert order items
		                insertOrderItems(con, formattedOrderNumber);

		                // Update stock
		                updateStock(con);

		                // Commit Transaction
		                con.commit();
		                System.out.println("Ending transaction...");
		                txtBcode.grabFocus();
		                // Print receipt
		             // Print receipt using a new thread (Java 7 version with anonymous class)
		                new Thread(new Runnable() {
		                    @Override
		                    public void run() {
		                        try {
		                        	btnPrintRcpt.setEnabled(false);
									printReceipt(config.comPort, formattedOrderNumber);
								} catch (Throwable e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
		                    }

							private void printReceipt(String comPort, String formattedOrderNumber) throws IOException, Exception, Throwable {
								// TODO Auto-generated method stub
								//btnPrintRcpt.setEnabled(false);
						        
						        //String numOrder = txtOrderNumber.getText();

						        /*if (numOrder.isEmpty()) {
						            JOptionPane.showMessageDialog(null, "Please save the order before closing it.");
						            return;
						        }*/

						        // Setup Serial Communication
						        sp = SerialPort.getCommPort(comPort);
						        sp.setComPortParameters(115200, 8, 1, 0);
						        sp.setFlowControl(0);
						        sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);

						        if (sp.openPort()) {
						            System.out.println("Connected to the fiscal printer.");

						            inputBuffer = sp.getInputStream();
						            outputBuffer = sp.getOutputStream();

						            mFMP = new FMP10TZA(inputBuffer, outputBuffer);
						            openFiscalRecipt0("1", "0000", "1");
						            
						            System.out.println("Starting to print");
						            System.out.println(model.getRowCount());
						            // Send receipt data
						            for (int count = 0; count < model.getRowCount(); count++) {
						            	System.out.println("row count is : " + count);
						                String desc = truncate(model.getValueAt(count, 1).toString(), 25);
						                System.out.println(desc);
						                String tax = model.getValueAt(count, 2).toString();
						                
						                String price = model.getValueAt(count, 4).toString();
						                String quantity = model.getValueAt(count, 3).toString();
						                sellItem(desc, tax, price, quantity);
						            }

						            // Finalize receipt
						            calcSumTotalPaidAmnt("P", txtTamount.getText());
						            totalCash();
						            closingFiscalReceipt();
						            disconnect();
						            
						         // Clear UI
					                clearUI();
								
							}
		                }}).start();		                
		                

		            } catch (SQLException e) {
		                // Rollback on error
		                e.printStackTrace();
		                JOptionPane.showMessageDialog(null, "Transaction failed: " + e.getMessage());
		            }
		        } catch (IOException e) {
		            e.printStackTrace();
		            JOptionPane.showMessageDialog(null, "Configuration loading failed: " + e.getMessage());
		        }
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
	
	private class change implements KeyListener
	{
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
				String tanderedAmount = txtTamount.getText();
				String stotal = txtTotal.getText();
				
				if (tanderedAmount.length()>0){
					double tandered = (Double.parseDouble(tanderedAmount));
					double dTotal = (Double.parseDouble(stotal));
		        	double changing = (tandered - dTotal);
		        	BigDecimal bdTandered = new BigDecimal(tandered);
		        	BigDecimal bdChanging = new BigDecimal(changing);
		        	BigDecimal bdTotal = new BigDecimal(dTotal);
		        	bdChanging = bdChanging.setScale(2, RoundingMode.HALF_UP);
		        	int comp = bdTandered.compareTo(bdTotal);
		        	if(comp>=0){
			        	txtChange.setText((bdChanging).toString());
			        	btnPrintRcpt.grabFocus();
			        	btnPrintRcpt.setEnabled(true);
		        	}
		        	else{
		        			JOptionPane.showMessageDialog(null, "Make sure customers tendered amount is enough");
		        			txtTamount.grabFocus();
		        			//btnPrintRcpt.setEnabled(false);
		        		}
				}
				else{
					JOptionPane.showMessageDialog(null, "Please enter the tendered amount");
					txtTamount.grabFocus();
					//btnPrintRcpt.setEnabled(false);
				}
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
	private boolean validateUser(String username, String password) {
        boolean isValid = false;
        try {
			File file = new File("C:\\libraries\\config.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String comPort = reader.readLine();
			String ip = reader.readLine();
			String user = reader.readLine();
			String pass = reader.readLine();
			String DBName = reader.readLine();
			String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
			try (Connection con = DriverManager.getConnection(connectionUrl);
		             PreparedStatement pst = con.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
		            pst.setString(1, username);
		            pst.setString(2, password);
		            try (ResultSet rs = pst.executeQuery()) {
		                isValid = rs.next();
		            }
		        } catch (SQLException ex) {
		            ex.printStackTrace();
		        }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return isValid;
    }
	private class Action implements ActionListener, KeyListener
	{
		protected Action()
		{
			
		}
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==btnVoid){
				// Prompt for username and password
                JTextField usernameField = new JTextField();
                JPasswordField passwordField = new JPasswordField();
                Object[] message = {
                        "Username:", usernameField,
                        "Password:", passwordField
                };

                int option = JOptionPane.showConfirmDialog(frame, message, "Authorization", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());

                    // Validate the username and password
                    if (validateUser(username, password)) {
                        // Proceed to delete the selected row
                        int row = tblSalesTable.getSelectedRow();
                        if (row != -1) {
                            model.removeRow(row);
                            double total = 0;
                            for (int count = 0; count < model.getRowCount(); count++) {
                                double price = Double.parseDouble(model.getValueAt(count, 4).toString());
                                double q = Double.parseDouble(model.getValueAt(count, 3).toString());
                                total = total + (price * q);
                            }

                            BigDecimal sbdPrice = new BigDecimal(total);
                            sbdPrice = sbdPrice.setScale(2, RoundingMode.HALF_UP);
                            txtTotal.setText(sbdPrice.toString());
                            txtChange.setText("");
                            txtBcode.grabFocus();
                            txtBcode.setText("");
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
			}
			
			if (e.getSource()==btnSearch){
				 if (e.getSource() == btnSearch) {
		                searchDialog.setVisible(true);
		            } /*if (e.getSource() == btnSelect) {
		                int selectedRow = tblSearchResult.getSelectedRow();
		                if (selectedRow != -1) {
		                    String sku = (String) resultModel.getValueAt(selectedRow, 0);
		                    String name = (String) resultModel.getValueAt(selectedRow, 1);
		                    String price = (String) resultModel.getValueAt(selectedRow, 2);
		                    model.addRow(new Object[]{sku, name, "", 1, price, price});
		                    searchDialog.setVisible(false);
		                }
		            }*/
				 
				/*try {
					resultModel.setRowCount(0);
					File file = new File("C:\\libraries\\config.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					reader.close();
									
					String code = txtSearch.getText();

					String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
					//JOptionPane.showMessageDialog(null, connectionUrl);
			        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[product] where [aroniumdb].[dbo].[product].name like \'%" +  code + "%\'";
			        	//String SQL = "select * from panda.dbo.productlist where name like \'%" +  code + "%\'";
			        	
			            ResultSet rs = stmt.executeQuery(SQL);
			            while(rs.next()){
			            	String SKU = rs.getString("id");
			            	String name = rs.getString("Name");
			            	String price = rs.getString("price");
			            	//int pric = Integer.parseInt(price);
			            	resultModel.addRow(new Object[] {SKU, name, price});
			            }
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				tblSearchResult.requestFocus();*/
				
			}						
			if (e.getSource()==btnSelect){
				try {
					File file = new File("C:\\libraries\\config.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					reader.close();
					int row = tblSearchResult.getSelectedRow();
					String code = tblSearchResult.getValueAt(row, 0).toString();
					String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
								        
					try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[product], [aroniumdb].[dbo].[ProductTax] where [aroniumdb].[dbo].[product].Id = \'" +  code + "\' and [aroniumdb].[dbo].[product].Id = [aroniumdb].[dbo].[ProductTax].ProductId";
			        	System.out.println(SQL);
			        	//String SQL = "select * from panda.dbo.productlist where SKU = \'" +  code + "\'";
			            //txtTamount.setText(SQL);
			            ResultSet rs = stmt.executeQuery(SQL);
			            while(rs.next()){
			            	String SKU = rs.getString("Id");
			            	String name = rs.getString("Name");
			            	String price = rs.getString("price");
			            	BigDecimal bdPrice = new BigDecimal(price);
			            	bdPrice = bdPrice.setScale(2, RoundingMode.HALF_UP);
			            	String taxID = "";
			            	BigDecimal vk_ebdPrice = new BigDecimal(price);
			            	vk_ebdPrice = vk_ebdPrice.setScale(2, RoundingMode.HALF_UP);
			            	
			            	if (rs.getString("TaxId").equals("2")){
			            		taxID = "A";
			            	}else{
			            		taxID = "B";
			            	}
			            	String quantity = "1";
			            	
			            	model.addRow(new Object[] {SKU, name, taxID, quantity, bdPrice, bdPrice});
			            	double total = 0;
							double change = 0.0;
							for (int count = 0; count < model.getRowCount(); count++){
						           double selling = Double.parseDouble(model.getValueAt(count, 3).toString());
						           double q = Double.parseDouble(model.getValueAt(count, 4).toString());
						           total = total + (selling * q);		           
						    }
							BigDecimal sbdPrice = new BigDecimal(total);
							sbdPrice = sbdPrice.setScale(2, RoundingMode.HALF_UP);
							txtTotal.setText((sbdPrice).toString());
							change = change - total;
							BigDecimal bdchange = new BigDecimal(change);
							bdchange = bdchange.setScale(2, RoundingMode.HALF_UP);
							txtChange.setText((bdchange).toString());	
							txtBcode.grabFocus();
			            }
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				resultModel.setRowCount(0);
				txtSearch.setText("");
				
			}
			//******reprint receipt*********************************
			if(e.getSource()==btnSaveOrder){
				File file = new File("C:\\libraries\\config.txt");
				BufferedReader reader;
				String dept = cmbdepartment.getSelectedItem().toString();
				String tableNumber = cmbTableNumber.getSelectedItem().toString();
				String section = cmbSection.getSelectedItem().toString();
				
				if (txtOrderNumber.getText().equals("")) {
			        // Existing code for processing new orders...
					try {
						reader = new BufferedReader(new FileReader(file));
						String comPort = reader.readLine();
						String ip = reader.readLine();
						String user = reader.readLine();
						String pass = reader.readLine();
						String DBName = reader.readLine();
						String till = reader.readLine();
						reader.close();
						
						String countOrders = "SELECT COUNT(dbo.Document.DocumentTypeId) as \"rows\" from Document where DocumentTypeId = 2";
						int numOrders = 0;
						String total = txtTotal.getText();
						String paddedOrders = "";
						LocalDate mydate = LocalDate.now();
						LocalDateTime dateTime = LocalDateTime.now();
						DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						String formatedDate = dateTime.format(ObjdateFormated);
						//SimpleDateFormat simpleDate = new SimpleDateFormat(mydate.toString());
						String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
						
						try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmtSt = con2.createStatement();) {
				        	ResultSet rsSt = stmtSt.executeQuery(countOrders);
				        	while(rsSt.next()){
				        		numOrders = rsSt.getInt("rows");
				        		System.out.println(numOrders);
				        		numOrders = numOrders + 1;
				        		paddedOrders = String.format("%06d", numOrders);
				        		System.out.println(numOrders);
				        		System.out.println(mydate);
				        		System.out.println(formatedDate);
				        		System.out.println(paddedOrders);
				        	}
				        	String userName = lblUsernameTxt.getText();
				        	String sessionID = lblSessionIDTxt.getText();
				        	String transactSQL = "insert into [aroniumdb].[dbo].[document] (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule, SectionId, TableId, DepartmentId, Status, sessionId) values ('19-200-"+paddedOrders +"','1', '1', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0, (SELECT Id FROM Section WHERE Section_Name = '" + section + "'), (SELECT Id FROM Tables WHERE Number = " + tableNumber +" ), (SELECT Id FROM Departments WHERE Department = '" + dept +"'), 'Open'," + sessionID + ")";
				        	System.out.println(transactSQL);
				        	stmtSt.executeUpdate(transactSQL);
				        	for (int count = 0; count < model.getRowCount(); count++){
						           String id = (model.getValueAt(count, 0).toString());
						           double QtySold = Double.parseDouble(model.getValueAt(count, 3).toString());
						           double salesTax = 0.0;
						           double priceAfterTax = Double.parseDouble(model.getValueAt(count, 4).toString());
						           double itemTotalAfterTax = Double.parseDouble(model.getValueAt(count, 5).toString());
						           double priceBeforeTax = 0.0;
						           double totalPriceBeforeTax = 0.0;
						           String tax = (model.getValueAt(count, 2).toString());
						           						           
						           if (tax.equals("A")){
						        	   salesTax = priceAfterTax * 0.165;
						        	   priceBeforeTax = priceAfterTax - salesTax;
						        	   itemTotalAfterTax = (itemTotalAfterTax - (salesTax * QtySold));
						           }
						           else{
						        	   priceBeforeTax = priceAfterTax;
						        	   itemTotalAfterTax = itemTotalAfterTax;
						           }
						           
						           String upDateDocItem = "insert into [aroniumdb].[dbo].[DocumentItem](DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
						           	 "((select id from aroniumdb.dbo.Document where OrderNumber =" + numOrders + ")," + id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0 )";
						           System.out.println(upDateDocItem);
						           stmtSt.executeUpdate(upDateDocItem);
				        	}
				        	Double DBsubTotal = 0.0;
							Double Total = 0.0;
							Double ItemTax = 0.0;
							char taxCode = 'A';
					        String taxCode2 = String.valueOf(taxCode);
					        Double taxTotal = 0.0;
					        Double taxableSales = 0.0;
					        Double nontaxableSales = 0.0;
					        String printText = "";
					        
					        try {
								int strSize = 0;
								try {
									Connect(comPort);
									String Tandered = txtTamount.getText();
									//String Pmode = "P";
									mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
									//openFiscalRecipt0("1", "0000", "1");
									openNonFiscalReceipt();
									printSeperatorLines("3");
									printBarcodeTypeWithoutNumber("1", "0000000");
									
									for (int count = 0; count < model.getRowCount(); count++){
										
								           String desc = ((model.getValueAt(count, 1).toString()));
								           strSize = desc.length();
								           if (strSize>25) {
								        	   desc = desc.substring(0, 25);
								           }
								           String tax = (model.getValueAt(count, 2).toString());
								           String price = (model.getValueAt(count, 4).toString());
								           String q = (model.getValueAt(count, 3).toString());
								           DBsubTotal = Double.parseDouble(price)*Double.parseDouble(q);
								           Total = Total + DBsubTotal;
								           if (tax.equals(taxCode2)){
								        	   ItemTax = ItemTax + (DBsubTotal * (16.5/100));
								        	   taxableSales = taxableSales + DBsubTotal;
								           }else{
								        	   ItemTax = ItemTax + 0.00;
								        	   nontaxableSales = nontaxableSales + DBsubTotal;
								           }
								           System.out.println("Subtatol is " + DBsubTotal.toString());
								           System.out.println(ItemTax);
								           System.out.println(tax);
								           printText =  desc + "   " + price + " X " + q + "  " + DBsubTotal +"  "+ tax;
								           printFreeFiscalText(printText);
									}
									
									printFreeFiscalText("------------------------------------------");
									/*BigDecimal sbCash = new BigDecimal(txtTamount.getText());
									sbCash = sbCash.setScale(2, RoundingMode.HALF_UP);
									BigDecimal sbTaxable = new BigDecimal(taxableSales);*/
									//sbTaxable = sbTaxable.setScale(2, RoundingMode.HALF_UP);
									BigDecimal sbNonTaxable = new BigDecimal(nontaxableSales);
									sbNonTaxable = sbNonTaxable.setScale(2, RoundingMode.HALF_UP);
									BigDecimal sbItemTax = new BigDecimal(ItemTax);
									sbItemTax = sbItemTax.setScale(2, RoundingMode.HALF_UP);
									String salesDetails = "";
									salesDetails = txtTotal.getText();
									//salesDetails += "Sales Taxable A    : " + sbTaxable +"\n" + "Tax Total          : " + sbItemTax +"\n" + "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
									printFreeFiscalText("Bill Total" + salesDetails);
									
									//printerServices.printString("POS-Printer", salesDetails);
									
										printFreeFiscalText("Enjoy!!!!!\n\n\n\n\n");						
										closeNonFiscalReceipt();
										disconnect();
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								} catch (IllegalArgumentException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
				        }
				        // Handle any errors that may have occurred.
				        catch (SQLException e1) {
				            e1.printStackTrace();
				        }
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						testNonFiscalPrint();
					} catch (IllegalArgumentException | IOException
							| InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					model.setRowCount(0);
                    txtTotal.setText("");
                    txtChange.setText("");
                    txtTamount.setText("");
                    txtOrderNumber.setText("");
                    txtBcode.grabFocus();
                    btnPrintRcpt.setEnabled(true);
			    }else{
			    	try {
						reader = new BufferedReader(new FileReader(file));
						String comPort = reader.readLine();
						String ip = reader.readLine();
						String user = reader.readLine();
						String pass = reader.readLine();
						String DBName = reader.readLine();
						String till = reader.readLine();
						reader.close();
						//txtOrderNumber.setEnabled(true);
						String orderNumber = txtOrderNumber.getText();
						//String getDocumentId = "SELECT Id FROM Document WHERE Number = \'" + orderNumber +"\'";
						int docId = 0;
						String total = txtTotal.getText();
						String paddedOrders = "";
						LocalDate mydate = LocalDate.now();
						LocalDateTime dateTime = LocalDateTime.now();
						DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
						String formatedDate = dateTime.format(ObjdateFormated);
						//SimpleDateFormat simpleDate = new SimpleDateFormat(mydate.toString());
						String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
								            
						try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmtSt = con2.createStatement();) {
				        	/*ResultSet rsSt = stmtSt.executeQuery(getDocumentId);
				        	while(rsSt.next()){
				        		docId = rsSt.getInt("Id");
				        		System.out.println(docId);				        		
				        	}*/
				        	String transactSQL = "UPDATE [aroniumdb].[dbo].[document] SET Total = " + total +" WHERE Id =" + orderNumber;
				        	System.out.println(transactSQL);
				        	stmtSt.executeUpdate(transactSQL);
					        
					       
					        String transactSQL2 = "DELETE FROM [aroniumdb].[dbo].[DocumentItem] WHERE DocumentId =" + orderNumber;
					        System.out.println(transactSQL2);
					        stmtSt.executeUpdate(transactSQL2);
					        
				        	for (int count = 0; count < model.getRowCount(); count++){
						           String id = (model.getValueAt(count, 0).toString());
						           double QtySold = Double.parseDouble(model.getValueAt(count, 3).toString());
						           double salesTax = 0.0;
						           double priceAfterTax = Double.parseDouble(model.getValueAt(count, 4).toString());
						           double itemTotalAfterTax = Double.parseDouble(model.getValueAt(count, 5).toString());
						           double priceBeforeTax = 0.0;
						           double totalPriceBeforeTax = 0.0;
						           String tax = (model.getValueAt(count, 2).toString());
						           
						           if (tax.equals("A")){
						        	   salesTax = priceAfterTax * 0.165;
						        	   priceBeforeTax = priceAfterTax - salesTax;
						        	   itemTotalAfterTax = (itemTotalAfterTax - (salesTax * QtySold));
						           }
						           else{
						        	   priceBeforeTax = priceAfterTax;
						        	   itemTotalAfterTax = itemTotalAfterTax;
						           }
						           
						           String upDateDocItem = "insert into [aroniumdb].[dbo].[DocumentItem](DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
						           	 "("+ orderNumber +"," + id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0 )";
						           System.out.println(upDateDocItem);
						           stmtSt.executeUpdate(upDateDocItem);
				        	}
				        	
				        }
				        // Handle any errors that may have occurred.
				        catch (SQLException e1) {
				            e1.printStackTrace();
				        }
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    	//Printing recept
			    	
					Double DBsubTotal = 0.0;
					Double Total = 0.0;
					Double ItemTax = 0.0;
					char taxCode = 'A';
			        String taxCode2 = String.valueOf(taxCode);
			        Double taxTotal = 0.0;
			        Double taxableSales = 0.0;
			        Double nontaxableSales = 0.0;
			        String printText = "";
			        
			        try {
					int strSize = 0;
					openNonFiscalReceipt();
					printSeperatorLines("3");
					printBarcodeTypeWithoutNumber("1", "0000000");
					
					for (int count = 0; count < model.getRowCount(); count++){
						
				           String desc = ((model.getValueAt(count, 1).toString()));
				           strSize = desc.length();
				           if (strSize>25) {
				        	   desc = desc.substring(0, 25);
				           }
				           String tax = (model.getValueAt(count, 2).toString());
				           String price = (model.getValueAt(count, 4).toString());
				           String q = (model.getValueAt(count, 3).toString());
				           DBsubTotal = Double.parseDouble(price)*Double.parseDouble(q);
				           Total = Total + DBsubTotal;
				           if (tax.equals(taxCode2)){
				        	   ItemTax = ItemTax + (DBsubTotal * (16.5/100));
				        	   taxableSales = taxableSales + DBsubTotal;
				           }else{
				        	   ItemTax = ItemTax + 0.00;
				        	   nontaxableSales = nontaxableSales + DBsubTotal;
				           }
				           System.out.println("Subtatol is " + DBsubTotal.toString());
				           System.out.println(ItemTax);
				           System.out.println(tax);
				           printText =  desc + "   " + price + " X " + q + "  " + DBsubTotal +"  "+ tax;
				           printFreeFiscalText(printText);
					}
					
					printFreeFiscalText("------------------------------------------");
					/*BigDecimal sbCash = new BigDecimal(txtTamount.getText());
					sbCash = sbCash.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbTaxable = new BigDecimal(taxableSales);*/
					//sbTaxable = sbTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbNonTaxable = new BigDecimal(nontaxableSales);
					sbNonTaxable = sbNonTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbItemTax = new BigDecimal(ItemTax);
					sbItemTax = sbItemTax.setScale(2, RoundingMode.HALF_UP);
					String salesDetails = "";
					salesDetails = txtTotal.getText();
					//salesDetails += "Sales Taxable A    : " + sbTaxable +"\n" + "Tax Total          : " + sbItemTax +"\n" + "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
					printFreeFiscalText("Bill Total" + salesDetails);
					
					//printerServices.printString("POS-Printer", salesDetails);
					
						printFreeFiscalText("Enjoy!!!!!\n\n\n\n\n");						
						closeNonFiscalReceipt();
						try {
							testNonFiscalPrint();
						} catch (IllegalArgumentException | IOException
								| InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					model.setRowCount(0);
					txtTotal.setText("");
					txtChange.setText("");
					txtTamount.setText("");
					txtBcode.grabFocus();
					btnPrintRcpt.setEnabled(false);	
			    	model.setRowCount(0);
                    txtTotal.setText("");
                    txtChange.setText("");
                    txtTamount.setText("");
                    txtOrderNumber.setText("");
                    txtBcode.grabFocus();
                    btnPrintRcpt.setEnabled(true);
			    }	
			}
			//******Recall last transaction  button code************
			if (e.getSource()==btnLastTrans){
				try {
					File file = new File("C:\\libraries\\config.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					String till = reader.readLine();
					reader.close();
					//int row = tblSearchResult.getSelectedRow();
					//String code = tblSearchResult.getValueAt(row, 0).toString();
					String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
					model.setRowCount(0);			        
					try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[DocumentItem], [aroniumdb].[dbo].[Product], [aroniumdb].[dbo].[ProductTax] WHERE DocumentId =(SELECT TOP 1[Id] FROM [aroniumdb].[dbo].[Document] WHERE Number LIKE '%19-200%' AND CashRegisterID = '" + till + "' ORDER BY Id DESC) AND [aroniumdb].[dbo].[DocumentItem].ProductId = [aroniumdb].[dbo].[Product].Id and [aroniumdb].[dbo].[Product].Id = [aroniumdb].[dbo].[ProductTax].ProductId";
			        	System.out.println(SQL);
			        	//String SQL = "select * from panda.dbo.productlist where SKU = \'" +  code + "\'";
			            //txtTamount.setText(SQL);
			            ResultSet rs = stmt.executeQuery(SQL);
			            while(rs.next()){
			            	String SKU = rs.getString("ProductID");
			            	String name = rs.getString("Name");
			            	Float price = rs.getFloat("price");
			            	Float qty = rs.getFloat("Quantity");
			            	BigDecimal bdPrice = new BigDecimal(price);
			            	bdPrice = bdPrice.setScale(2, RoundingMode.HALF_UP);
			            	String taxID = "";
			            	float subTotal = 0;
			            	subTotal = qty * price;
			            	BigDecimal bdSubTotal = new BigDecimal(subTotal);
			            	bdSubTotal = bdSubTotal.setScale(2, RoundingMode.HALF_UP);
			            	if (rs.getString("TaxId").equals("2")){
			            		taxID = "A";
			            	}else{
			            		taxID = "B";
			            	}
			            	String quantity = "1";
			            	double modelQty = 0.0;
			            	//int pric = Integer.parseInt(price);
			            	for (int count = 0; count < model.getRowCount(); count++){
			            			String modelSKU = model.getValueAt(count, 0).toString();
			            			
			            			System.out.println("the model quantity was" + modelQty);
			            			double modelPrice = Double.parseDouble(model.getValueAt(count, 4).toString());
			            			double accumulativeTotal = modelQty * modelPrice;
			            			//System.out.println(modelQty);
			            			/*if (modelSKU.equals(SKU)){
			            				modelQty = Double.parseDouble(model.getValueAt(count, 3).toString());
			            				modelQty = modelQty + 1;
			            				System.out.println(modelQty);
			            				System.out.println(count);
			            			}*/
						           //double selling = Double.parseDouble(model.getValueAt(count, 3).toString());
						           //double q = Double.parseDouble(model.getValueAt(count, 4).toString());
						           //total = total + (selling * q);	
			            			
						    }
			            	model.addRow(new Object[] {SKU, name, taxID, qty, bdPrice, bdSubTotal});
			            	double total = 0;
							double change = 0.0;
							for (int count = 0; count < model.getRowCount(); count++){
						           double selling = Double.parseDouble(model.getValueAt(count, 3).toString());
						           double q = Double.parseDouble(model.getValueAt(count, 4).toString());
						           total = total + (selling * q);		           
						    }
							BigDecimal sbdPrice = new BigDecimal(total);
							sbdPrice = sbdPrice.setScale(2, RoundingMode.HALF_UP);
							txtTotal.setText((sbdPrice).toString());
							change = change - total;
							BigDecimal bdchange = new BigDecimal(change);
							bdchange = bdchange.setScale(2, RoundingMode.HALF_UP);
							txtChange.setText((bdchange).toString());	
							txtBcode.grabFocus();
			            }
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			if (e.getSource()==btnlogoff){
				try {
					File file = new File("C:\\libraries\\config.txt");
					@SuppressWarnings("resource")
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					String till = reader.readLine();
					int numOrders = 0;
					String UserName = lblUsernameTxt.getText();
					String closeSession = "UPDATE user_sessions SET Status = \'Closed\' WHERE User_Id =(SELECT Id FROM Users WHERE Username = \'" + UserName +"\')";
					System.out.println(closeSession);
					String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
					try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmtSt = con2.createStatement();) {
			        	stmtSt.executeUpdate(closeSession);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					frame.dispose();
				}catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			/***if (e.getSource()==btnPrintRcpt){
				try {
					File file = new File("C:\\libraries\\config.txt");
					@SuppressWarnings("resource")
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					String till = reader.readLine();
					String countOrders = "SELECT COUNT(dbo.Document.DocumentTypeId) as \"rows\" from Document where DocumentTypeId = 2";
					int numOrders = 0;
					String total = txtTotal.getText();
					String paddedOrders = "";
					LocalDate mydate = LocalDate.now();
					LocalDateTime dateTime = LocalDateTime.now();
					DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String formatedDate = dateTime.format(ObjdateFormated);
					//SimpleDateFormat simpleDate = new SimpleDateFormat(mydate.toString());
					String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
					try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmtSt = con2.createStatement();) {
			        	ResultSet rsSt = stmtSt.executeQuery(countOrders);
			        	while(rsSt.next()){
			        		numOrders = rsSt.getInt("rows");
			        		System.out.println(numOrders);
			        		numOrders = numOrders + 1;
			        		paddedOrders = String.format("%06d", numOrders);
			        		System.out.println(numOrders);
			        		System.out.println(mydate);
			        		System.out.println(formatedDate);
			        		System.out.println(paddedOrders);
			        	}
			        	String userName = lblUsernameTxt.getText();
			        	System.out.println("this the user name " + userName);
			        	String sessionID = lblSessionIDTxt.getText();
			        	String transactSQL = "insert into [aroniumdb].[dbo].[document] (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule, sessionId) values ('19-200-"+paddedOrders +"','1', '1', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0," + sessionID +")";
			        	System.out.println(transactSQL);
			        	stmtSt.executeUpdate(transactSQL);
			        	for (int count = 0; count < model.getRowCount(); count++){
					           String id = (model.getValueAt(count, 0).toString());
					           double QtySold = Double.parseDouble(model.getValueAt(count, 3).toString());
					           double salesTax = 0.0;
					           double priceAfterTax = Double.parseDouble(model.getValueAt(count, 4).toString());
					           double itemTotalAfterTax = Double.parseDouble(model.getValueAt(count, 5).toString());
					           double priceBeforeTax = 0.0;
					           double totalPriceBeforeTax = 0.0;
					           String tax = (model.getValueAt(count, 2).toString());
					           if (tax.equals("A")){
					        	   salesTax = priceAfterTax * 0.165;
					        	   priceBeforeTax = priceAfterTax - salesTax;
					        	   itemTotalAfterTax = (itemTotalAfterTax - (salesTax * QtySold));
					           }
					           else{
					        	   priceBeforeTax = priceAfterTax;
					        	   itemTotalAfterTax = itemTotalAfterTax;
					           }
					           String upDateDocItem = "insert into [aroniumdb].[dbo].[DocumentItem](DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
					           	 "((select id from aroniumdb.dbo.Document where OrderNumber ='" + numOrders + "')," + id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0)";
					           System.out.println(upDateDocItem);
					           stmtSt.executeUpdate(upDateDocItem);
			        	}	            
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
					for (int count = 0; count < model.getRowCount(); count++){
				           String id = (model.getValueAt(count, 0).toString());
				           double sale = Double.parseDouble(model.getValueAt(count, 3).toString());
				           String SQL = "SELECT * FROM [aroniumdb].[dbo].[stock] where productId =" +  id;
						   System.out.println(SQL);
				           try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
					        	//String SQL = "SELECT * FROM [aroniumdb].[dbo].[stock] where Id = " +  id ;
					        	ResultSet rs = stmt.executeQuery(SQL);
					        	if (rs.next()==false){
						          float stock = -1; //rs.getFloat("Quantity");
						          System.out.println(stock);
						          //JOptionPane.showMessageDialog(null, Double.toString(stock));
						          double balance = stock; //- sale;
							          try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmt2 = con2.createStatement();) {
									    	String updateSQL = "insert into [aroniumdb].[dbo].[stock] (productID, warehouseId, Quantity) values (" +  id + "," + 1 +", " + balance + ")" ;
									       	System.out.println(updateSQL);
									    	stmt2.executeUpdate(updateSQL);  
									  }
									  // Handle any errors that may have occurred.
									  catch (SQLException e1) {
									      e1.printStackTrace();
									  }
						         }else{
						        	 System.out.println(sale);
					        		//while(rs.next()){
						            	float stock = rs.getFloat("Quantity");
						            	System.out.println(stock);
						            	
						            	//JOptionPane.showMessageDialog(null, Double.toString(stock));
						            	double balance = stock - sale;
						            	try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmt2 = con2.createStatement();) {
								        	String updateSQL = "update [aroniumdb].[dbo].[stock] set Quantity = " + balance + "where ProductId =" +  id;
								        	stmt2.executeUpdate(updateSQL);
								            
								        }
								        // Handle any errors that may have occurred.
								        catch (SQLException e1) {
								            e1.printStackTrace();
								        }
						            	
						            //}
					        	}
					        }
					        // Handle any errors that may have occurred.
					        catch (SQLException e1) {
					            e1.printStackTrace();
					        }
				           
				    }
					//Connect(comPort);
					sp = SerialPort.getCommPort(comPort);
					
					sp.setComPortParameters(115200, 8, 1, 0);
					sp.setFlowControl(0);
					sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
							
					if(sp.openPort()==true)
					{
									             
			            inputBuffer = sp.getInputStream();
			            Thread.sleep(800);
			            outputBuffer = sp.getOutputStream();
			        
			            Thread.sleep(800);
			            (new Thread(new SerialWriter(outputBuffer))).start();
			            Thread.sleep(800);
			            (new Thread(new SerialReader(inputBuffer))).start();
			            
			            Thread.sleep(2000);
			            System.out.println("Connected");
			            String Tandered = txtTamount.getText();
						String Pmode = "P";
						mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
						openFiscalRecipt0("1", "0000", "1");	
						//String customer = txtCustomer.getText();
						//printFreeFiscalText("Cusoumer Name: " + customer);
						int strSize = 0;
						for (int count = 0; count < model.getRowCount(); count++){
							
					           String desc = ((model.getValueAt(count, 1).toString()));
					           strSize = desc.length();
					           if (strSize>25) {
					        	   desc = desc.substring(0, 25);
					           }
					           String tax = (model.getValueAt(count, 2).toString());
					           String price = (model.getValueAt(count, 4).toString());
					           String q = (model.getValueAt(count, 3).toString());
					           sellItem(desc, tax, price, q);
							   
					    }
						calcSumTotalPaidAmnt(Pmode, Tandered);
						totalCash();
						closingFiscalReceipt();
						disconnect();
						model.setRowCount(0);
						txtTotal.setText("");
						txtChange.setText("");
						txtTamount.setText("");
						//txtCustomer.setText("");
						txtBcode.grabFocus();
						btnPrintRcpt.setEnabled(false);
					}
					else
					{
						sp.closePort();
						System.out.println("Failed...");
						JOptionPane.showMessageDialog(null, "Failed to connect to printer");
						sp = SerialPort.getCommPort(comPort);
						
						sp.setComPortParameters(115200, 8, 1, 0);
						sp.setFlowControl(0);
						sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
								
						if(sp.openPort()==true)
						{
										             
				            inputBuffer = sp.getInputStream();
				            Thread.sleep(1500);
				            outputBuffer = sp.getOutputStream();
				        
				            Thread.sleep(800);
				            (new Thread(new SerialWriter(outputBuffer))).start();
				            Thread.sleep(800);
				            (new Thread(new SerialReader(inputBuffer))).start();
				            
				            Thread.sleep(800);
				            System.out.println("Connected");
				            String Tandered = txtTamount.getText();
							String Pmode = "P";
							mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
							openFiscalRecipt0("1", "0000", "1");					
							int strSize = 0;
							for (int count = 0; count < model.getRowCount(); count++){
								
						           String desc = ((model.getValueAt(count, 1).toString()));
						           strSize = desc.length();
						           if (strSize>25) {
						        	   desc = desc.substring(0, 25);
						           }
						           String tax = (model.getValueAt(count, 2).toString());
						           String price = (model.getValueAt(count, 4).toString());
						           String q = (model.getValueAt(count, 3).toString());
						           sellItem(desc, tax, price, q);
								   
						    }
							calcSumTotalPaidAmnt(Pmode, Tandered);
							totalCash();
							closingFiscalReceipt();
							disconnect();
							model.setRowCount(0);
							txtTotal.setText("");
							txtChange.setText("");
							txtTamount.setText("");
							txtBcode.grabFocus();
							btnPrintRcpt.setEnabled(false);
						}
						else
						{
							sp.closePort();
							System.out.println("Failed...");
							JOptionPane.showMessageDialog(null, "Failed to connect to printer");
						}
					}
					
					//model.addRow(new Object[] {"Description", "TAX ID", "Quantity", "Price"});
								
				} catch (IllegalArgumentException | IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					JOptionPane.showMessageDialog(null,e2);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,e1);
				} catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,e1);
				}				
			
				
			}**/
			/**if (e.getSource()==btnPrintRcpt){
				try {
					File file = new File("C:\\libraries\\config.txt");
					@SuppressWarnings("resource")
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					String till = reader.readLine();
					String countOrders = "SELECT COUNT(dbo.Document.DocumentTypeId) as \"rows\" from Document where DocumentTypeId = 2";
					int numOrders = 0;
					String total = txtTotal.getText();
					String numOrder = txtOrderNumber.getText();
					String paddedOrders = "";
					LocalDate mydate = LocalDate.now();
					LocalDateTime dateTime = LocalDateTime.now();
					DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String formatedDate = dateTime.format(ObjdateFormated);
					//SimpleDateFormat simpleDate = new SimpleDateFormat(mydate.toString());
					if(txtOrderNumber.getText().equals("")){
						JOptionPane.showMessageDialog(null, "Please save the orderbefore closing it");
						
					}else{
						
						//Connect(comPort);
						sp = SerialPort.getCommPort(comPort);
						
						sp.setComPortParameters(115200, 8, 1, 0);
						sp.setFlowControl(0);
						sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);
								
						if(sp.openPort()==true)
						{
										             
				            inputBuffer = sp.getInputStream();
				            Thread.sleep(2000);
				            outputBuffer = sp.getOutputStream();
				        
				            Thread.sleep(2000);
				            (new Thread(new SerialWriter(outputBuffer))).start();
				            Thread.sleep(2000);
				            (new Thread(new SerialReader(inputBuffer))).start();
				            
				            Thread.sleep(2000);
				            System.out.println("Connected");
				            String Tandered = txtTamount.getText();
							String Pmode = "P";
							mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
							openFiscalRecipt0("1", "0000", "1");	
							//String customer = txtCustomer.getText();
							//printFreeFiscalText("Cusoumer Name: " + customer);
							int strSize = 0;
							for (int count = 0; count < model.getRowCount(); count++){
								
						           String desc = ((model.getValueAt(count, 1).toString()));
						           strSize = desc.length();
						           if (strSize>25) {
						        	   desc = desc.substring(0, 25);
						           }
						           String tax = (model.getValueAt(count, 2).toString());
						           String price = (model.getValueAt(count, 4).toString());
						           String q = (model.getValueAt(count, 3).toString());
						           sellItem(desc, tax, price, q);
								   
						    }
							
							
							calcSumTotalPaidAmnt(Pmode, Tandered);
							totalCash();
							closingFiscalReceipt();
							disconnect();
							
							String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
							
					        
							model.setRowCount(0);
							txtTotal.setText("");
							txtChange.setText("");
							txtTamount.setText("");
							txtOrderNumber.setText("");
							txtBcode.grabFocus();
							btnPrintRcpt.setEnabled(false);
							
							try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
					        	String SQL = "UPDATE Document SET Status = Closed WHERE Id =" +  numOrder;
					        	System.out.println(SQL);
					        }
							
							
						}
						else
						{
							sp.closePort();
							System.out.println("Failed...");
							JOptionPane.showMessageDialog(null, "Failed to connect to printer");
							sp = SerialPort.getCommPort(comPort);
							
							sp.setComPortParameters(115200, 8, 1, 0);
							sp.setFlowControl(0);
							sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);
									
							if(sp.openPort()==true)
							{
											             
					            inputBuffer = sp.getInputStream();
					            Thread.sleep(1500);
					            outputBuffer = sp.getOutputStream();
					        
					            Thread.sleep(1500);
					            (new Thread(new SerialWriter(outputBuffer))).start();
					            Thread.sleep(1500);
					            (new Thread(new SerialReader(inputBuffer))).start();
					            
					            Thread.sleep(800);
					            System.out.println("Connected");
					            String Tandered = txtTamount.getText();
								String Pmode = "P";
								mFMP = new FMP10TZA(sp.getInputStream(), sp.getOutputStream());	
								openFiscalRecipt0("1", "0000", "1");					
								int strSize = 0;
								for (int count = 0; count < model.getRowCount(); count++){
									
							           String desc = ((model.getValueAt(count, 1).toString()));
							           strSize = desc.length();
							           if (strSize>25) {
							        	   desc = desc.substring(0, 25);
							           }
							           String tax = (model.getValueAt(count, 2).toString());
							           String price = (model.getValueAt(count, 4).toString());
							           String q = (model.getValueAt(count, 3).toString());
							           sellItem(desc, tax, price, q);
									   
							    }
								calcSumTotalPaidAmnt(Pmode, Tandered);
								totalCash();
								closingFiscalReceipt();
								disconnect();
								
								model.setRowCount(0);
								txtTotal.setText("");
								txtChange.setText("");
								txtTamount.setText("");
								txtBcode.grabFocus();
								btnPrintRcpt.setEnabled(false);
								
								String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
								
								try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
						        	String SQL = "UPDATE Document SET Status = Closed WHERE Id =" +  numOrder;
						        	System.out.println(SQL);
						        }
							}
							else
							{
								sp.closePort();
								System.out.println("Failed...");
								JOptionPane.showMessageDialog(null, "Failed to connect to printer");
							}
						}
						
					}
					
					
					//model.addRow(new Object[] {"Description", "TAX ID", "Quantity", "Price"});
								
				} catch (IllegalArgumentException | IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					JOptionPane.showMessageDialog(null,e2);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,e1);
				} catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,e1);
				}				
			}**/
			if (e.getSource() == btnPrintRcpt) {
			    /**try {
			        // Load configuration once and cache it (assuming the method reads and stores the config in memory).
			        Map<String, String> config = loadConfig("C:\\libraries\\config.txt");
			        String comPort = config.get("comPort");
			        String ip = config.get("ip");
			        String user = config.get("user");
			        String pass = config.get("pass");
			        String DBName = config.get("DBName");

			        String total = txtTotal.getText();
			        System.out.println(total);
			        //String numOrder = txtOrderNumber.getText();

			        /*if (numOrder.isEmpty()) {
			            JOptionPane.showMessageDialog(null, "Please save the order before closing it.");
			            return;
			        }*/

			        // Setup Serial Communication
			       /** sp = SerialPort.getCommPort(comPort);
			        sp.setComPortParameters(115200, 8, 1, 0);
			        sp.setFlowControl(0);
			        sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 2000, 0);

			        if (sp.openPort()) {
			            System.out.println("Connected to the fiscal printer.");

			            inputBuffer = sp.getInputStream();
			            outputBuffer = sp.getOutputStream();

			            mFMP = new FMP10TZA(inputBuffer, outputBuffer);
			            openFiscalRecipt0("1", "0000", "1");

			            // Send receipt data
			            for (int count = 0; count < model.getRowCount(); count++) {
			                String desc = truncate(model.getValueAt(count, 1).toString(), 25);
			                String tax = model.getValueAt(count, 2).toString();
			                String price = model.getValueAt(count, 4).toString();
			                String quantity = model.getValueAt(count, 3).toString();
			                sellItem(desc, tax, price, quantity);
			            }

			            // Finalize receipt
			            calcSumTotalPaidAmnt("P", txtTamount.getText());
			            totalCash();
			            closingFiscalReceipt();
			            disconnect();

			            // Update order status in database
			            //updateOrderStatus(ip, DBName, user, pass, numOrder);

			            // Clear the UI for the next order


			        } else {
			            System.out.println("Failed to connect to the fiscal printer.");
			            JOptionPane.showMessageDialog(null, "Failed to connect to printer.");
			        }
			    } catch (Exception ex) {
			        ex.printStackTrace();
			        JOptionPane.showMessageDialog(null, ex.getMessage());
			    } catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}**/
			}		
		}
		/**
		 * Utility methods for modular code
		 */

		// Load configuration file once and reuse it
		private Map<String, String> loadConfig(String filePath) throws IOException {
		    Map<String, String> config = new HashMap<>();
		    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
		        config.put("comPort", reader.readLine());
		        config.put("ip", reader.readLine());
		        config.put("user", reader.readLine());
		        config.put("pass", reader.readLine());
		        config.put("DBName", reader.readLine());
		    }
		    return config;
		}
		// Truncate long descriptions
					private String truncate(String text, int maxLength) {
					    return (text.length() > maxLength) ? text.substring(0, maxLength) : text;
					}
		// Update order status in the database
		private void updateOrderStatus(String ip, String dbName, String user, String pass, String orderId) {
		    String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + dbName + ";user=" + user + ";password=" + pass;
		    try (Connection con = DriverManager.getConnection(connectionUrl);
		    	Statement stmt = con.createStatement()) {
		        String SQL = "UPDATE Document SET Status = 'Closed' WHERE Id = " + orderId;
				stmt.executeUpdate(SQL);
				System.out.println("Order " + orderId + " status updated.");
			} catch (SQLException e) {
			  e.printStackTrace();
			  JOptionPane.showMessageDialog(null, "Database update failed: " + e.getMessage());
			}
		}
		// Clear UI fields after printing
		private void resetUI() {
		    model.setRowCount(0);
		    txtTotal.setText("");
		    txtChange.setText("");
		    txtTamount.setText("");
		    //txtOrderNumber.setText("");
		    txtBcode.grabFocus();
		    btnPrintRcpt.setEnabled(false);
		}

		@Override
		public void keyPressed(java.awt.event.KeyEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getKeyCode()==KeyEvent.VK_F2){
				//JOptionPane.showMessageDialog(null, "it work");
				txtTamount.grabFocus();				
			}
			if (arg0.getKeyCode()==KeyEvent.VK_F3){
				txtSearch.grabFocus();				
			}
			if (arg0.getKeyCode()==KeyEvent.VK_F4){
				/*int rowNum = tblSearchResult.getSelectedRow();
				String selectedPLU = resultModel.getValueAt(0, rowNum).toString();
				JOptionPane.showMessageDialog(null, selectedPLU);*/
				//btnSaveOrder.grabFocus();
			}
			if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
				String code = txtBcode.getText();
				if(code==""){
					JOptionPane.showMessageDialog(null, "Please enter barcode"); 
				}else{
						try {
							//JOptionPane.showMessageDialog(null, "It works");
							File file = new File("C:\\libraries\\config.txt");
							BufferedReader reader = new BufferedReader(new FileReader(file));
							String comPort = reader.readLine();
							String ip = reader.readLine();
							String user = reader.readLine();
							String pass = reader.readLine();
							String DBName = reader.readLine();
							reader.close();						
							String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
					        String PIDt ="";
					        String quantity = "1";
					        PIDt = code.substring(0,2);
					        double subtotal = 0.0;			        
					        /*if (PIDt.equals("20")){
					        	quantity = code.substring(7,9) + "." + code.substring(9,12);
					        	code = code.substring(2,6);
					        	//JOptionPane.showMessageDialog(null, code);
					        }*/
					        //JOptionPane.showMessageDialog(null, PIDt);
							try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
					        	String SQL = "SELECT * FROM [aroniumdb].[dbo].[product], [aroniumdb].[dbo].[barcode], [aroniumdb].[dbo].[ProductTax] where [aroniumdb].[dbo].[product].Id = [aroniumdb].[dbo].[barcode].ProductId and [aroniumdb].[dbo].[Product].Id = [aroniumdb].[dbo].[ProductTax].ProductId and [aroniumdb].[dbo].[Barcode].Value = \'" +  code + "\'";
					        	//txtTamount.setText(SQL);
					        	//String SQL = "select * from panda.dbo.productlist where Barcode = \'" +  code + "\'";
					        	//txtTamount.setText(SQL);
					            ResultSet rs = stmt.executeQuery(SQL);
					            if (rs.next()==false){
					            	JOptionPane.showMessageDialog(null, "Product not found"); 
						        }
						        else{
						            //
						        	//while(rs.next()){
						            	String SKU = rs.getString("Id");
						            	String name = rs.getString("Name");
						            	double price = rs.getDouble("price");
						            	String taxID = "";
						            	String showTID = rs.getString("TaxId");
						            	BigDecimal vk_ebdPrice = new BigDecimal(price);
						            	vk_ebdPrice = vk_ebdPrice.setScale(2, RoundingMode.HALF_UP);
						            	double q = 1.0;
						            	if (rs.getString("TaxId").equals("2")){
						            		taxID = "A";
						            	}else{
						            		taxID = "B";
						            	}
						            	//JOptionPane.showMessageDialog(null, taxID);
						            	//int pric = Integer.parseInt(price);
						            	
						            	int numRows = 0;
						            	numRows = model.getRowCount();
						            	if (numRows == 0){
						            		subtotal = Double.parseDouble(quantity) * price;
						            	}
						            	else{
						            		for (int count = 0; count < numRows; count++){
												   q = Double.parseDouble(model.getValueAt(count, 3).toString());
										           double cq = Double.parseDouble(quantity);
										           String CompSKU = model.getValueAt(count, 0).toString();
										           if (CompSKU.equals(SKU)){
										        	  q = Double.sum(q, cq);
										        	  subtotal = q * price;
										        	  model.removeRow(count);
										        	  numRows = numRows - 1;
										        	  quantity = Double.toString(q);
										           }
										           else{
										        	   subtotal = Double.parseDouble(quantity) * price; 
										           }
										           //total = total + (selling * q);
										    }
						            	}
						            	
						            	BigDecimal bdsubtotal = new BigDecimal(subtotal);
							        	bdsubtotal = bdsubtotal.setScale(2, RoundingMode.HALF_UP);
						            	model.addRow(new Object[] {SKU, name, taxID, quantity, vk_ebdPrice, bdsubtotal});
						            	double total = 0;
										double change = 0.0;
										q = 1.0;
										for (int count = 0; count < model.getRowCount(); count++){
											   double selling = Double.parseDouble(model.getValueAt(count, 3).toString());
									           double qty = Double.parseDouble(model.getValueAt(count, 4).toString());
									           total = total + (selling * qty);
									    }
										BigDecimal sbdPrice = new BigDecimal(total);
										sbdPrice = sbdPrice.setScale(2, RoundingMode.HALF_UP);
										txtTotal.setText((sbdPrice).toString());
										change = change - total;
										BigDecimal bdchange = new BigDecimal(change);
										bdchange = bdchange.setScale(2, RoundingMode.HALF_UP);
										txtChange.setText((bdchange).toString());
						            }
						        //}
							}
					        // Handle any errors that may have occurred.
					        catch (SQLException e) {
					            e.printStackTrace();
					        }
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				        txtBcode.setText("");
					}
				
					
				}
							
		}
		@Override
		public void keyReleased(java.awt.event.KeyEvent arg0) {
			// TODO Auto-generated method stub
	
		}
		@Override
		public void keyTyped(java.awt.event.KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	/*public static void disconnect() 
    {
		sp.closePort();   
		System.out.println("Disconnected");
    }
      
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
    		System.err.println("Serial Reader...");
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            
            int len = -1;
            
            try
            {
        		System.err.println("Serial READER...");
                while ( ( len = in.read(buffer)) > 0 )
                {
                    System.out.println("Received data...");
                    System.out.print(new String(buffer,0,len));
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }

    
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
    		System.err.println("Serial Writer...");
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
        		System.err.println("Serial Writer...");

                while ( ( c = System.in.read()) > -1 )
                {
            		System.err.print(c);
                    this.out.write(c);
                    TimeUnit.MICROSECONDS.sleep(100);
                }                
        		System.err.println("Serial Writer..Done.");
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
        }
    }

	 public static void Connect(String portName) throws Exception{

		 	System.out.println("Connect COMPort: " + portName);
		 
		 	sp = SerialPort.getCommPort(portName);
			
			sp.setComPortParameters(115200, 8, 1, 0);
			sp.setFlowControl(0);
			sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
					
			if(sp.openPort()==true)
			{
							             
	            inputBuffer = sp.getInputStream();
	            Thread.sleep(800);
	            outputBuffer = sp.getOutputStream();
	        
	            Thread.sleep(800);
	            (new Thread(new SerialWriter(outputBuffer))).start();
	            Thread.sleep(800);
	            (new Thread(new SerialReader(inputBuffer))).start();
	            
	            Thread.sleep(800);
	            System.out.println("Connected");
			}
			else
			{
				//sp.closePort();
				System.out.println("Failed...");
				JOptionPane.showMessageDialog(null, "Failed to connect to printer");
				sp.closePort();
			}
     } */
	public static void disconnect() 
    {
		sp.closePort();   
		System.out.println("Disconnected");
    }
      
    public static class SerialReader implements Runnable 
    {
        InputStream in;
        
        public SerialReader ( InputStream in )
        {
    		System.err.println("Serial Reader...");
            this.in = in;
        }
        
        public void run ()
        {
            byte[] buffer = new byte[1024];
            
            int len = -1;
            
            try
            {
        		System.err.println("Serial READER...");
                while ( ( len = in.read(buffer)) > 0 )
                {
                    System.out.println("Received data...");
                    System.out.print(new String(buffer,0,len));
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }            
        }
    }

    
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
    		System.err.println("Serial Writer...");
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                int c = 0;
        		System.err.println("Serial Writer...");

                while ( ( c = System.in.read()) > -1 )
                {
            		System.err.print(c);
                    this.out.write(c);
                    TimeUnit.MICROSECONDS.sleep(100);
                }                
        		System.err.println("Serial Writer..Done.");
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
        }
    }

	 public static void Connect(String portName) throws Exception{

		 	System.out.println("Connect COMPort: " + portName);
		 
		 	sp = SerialPort.getCommPort(portName);
			
			sp.setComPortParameters(115200, 8, 1, 0);
			sp.setFlowControl(0);
			sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 800, 0);
					
			if(sp.openPort()==true)
			{
							             
	            inputBuffer = sp.getInputStream();
	            Thread.sleep(800);
	            outputBuffer = sp.getOutputStream();
	        
	            Thread.sleep(800);
	            (new Thread(new SerialWriter(outputBuffer))).start();
	            Thread.sleep(800);
	            (new Thread(new SerialReader(inputBuffer))).start();
	            
	            Thread.sleep(800);
	            System.out.println("Connected");
			}
			else
			{
				//sp.closePort();
				System.out.println("Failed...");
				JOptionPane.showMessageDialog(null, "Failed to connect to printer");
				sp.closePort();
			}
     }

	/*public UI1(String portName) throws Exception {
		
	}*/
	
	public  void testNonFiscalPrint() throws IllegalArgumentException, IOException, InterruptedException
	{
		openNonFiscalReceipt();
		printSeperatorLines("3");
		printBarcodeTypeWithoutNumber("1", "0000000");
		printFreeFiscalText(".....* SIMPLE NON FISCAL TEXT..... *");
		closeNonFiscalReceipt();
	}

	public  void testFiscalPrint() throws Exception, Throwable
	{
		//System.out.print("NonFIscalPRint...\r\n\r\n\r\n");
		openFiscalRecipt0("1", "0000", "1");
		//printBarcodeTypeWithoutNumber("1", "0000000");
		printBarcodeTypeWithNumber("1", "0000000");
		printFreeFiscalText("    * SIMPLE FISCAL TEXT *");
		sellItem("ITEM 1", "B", "0.99", "1");
		sellItem("ITEM 2", "B", "1.99", "1");
		sellItem("ITEM 3", "B", "2.99", "1");
		totalCash();
		closingFiscalReceipt();
	}

	public void totalCash() throws IOException
	{
		mFMP.totalInCash();
	}
	
	public void sellItem (String SaleDescription, String TaxCd, String SinglePrice, String Qwantity) throws IOException, Exception, Throwable
	{
		mFMP.sellThis(SaleDescription, TaxCd, SinglePrice, Qwantity);
	}

	/********************************************/
	public  void openNonFiscalReceipt() throws IllegalArgumentException, IOException, InterruptedException
	{
		//26h (38) Open a non-fiscal receipt.
		mFMP.cmd38v0b0();
	}

	public  void closeNonFiscalReceipt() throws IllegalArgumentException, IOException, InterruptedException
	{
		mFMP.cmd39v0b0();
	}
	
	public  void printNonFiscalText(String TRGT_TEXT) throws IllegalArgumentException, IOException, InterruptedException
	{
		mFMP.cmd42v0b0(TRGT_TEXT);
	}
	
	/********************************************/

	public  void openFiscalRecipt0(String OpCode, String OpPwd, String TillNmb) throws  IllegalArgumentException, IOException
	{
		/*
		 * cmd48v0b0(String OpCode, String OpPwd, String TillNmb) 
		public FiscalResponse  cmd48v0b0 (String OpCode, String OpPwd, String TillNmb) 
		 30h (48) OPENING A FISCAL CLIENT'S RECEIPT 
		Parameters
		OpCode				Operator's number (1 to 16)
		OpPwd				Operator's password (4 to 8 digits)
		TillNmb				Number of point of sale (a whole number of maximum 5 digits)
		*/
		mFMP.cmd48v0b0(OpCode, OpPwd, TillNmb);

	}
		
	
	public  void openFiscalRecipt1(String OpCode, String OpPwd, String TillNmb, String BuyerLine1, String BuyerLine2, String BuyerLine3, String TIN, String VRN) 
			throws  IllegalArgumentException, IOException
	{
		
		/*
		 * 
			30h (48) OPENING A FISCAL CLIENT'S RECEIPT 
			Parameters
		OpCode			Operator's number (1 to 16)
 		OpPwd			Operator's password (4 to 8 digits)
 		TillNmb			Number of point of sale (a whole number of maximum 5 digits)
 		BuyerLine1		Buyer's line info
 		BuyerLine2		Buyer's line info
 		BuyerLine3		Buyer's line info
 		TIN				Buyers tax identification number
 		VRN				Buyers VAT identification number 
		 */
	
		mFMP.cmd48v0b1(OpCode, OpPwd, TillNmb, BuyerLine1, BuyerLine2, BuyerLine3, TIN, VRN);
	}

	/**********************************************************/
	
	public  void registerSalePerc(String L1, String L2, String TaxCd, String Price, String Quan, String UN, String Perc) throws  IllegalArgumentException, IOException	
	{
		/*
	L1		A text of up to 36 bytes containing one line of description of sale.
 	L2		A text of up to 36 bytes containing a second line describing the sale.
 	TaxCd	One byte containing letter, which indicates the type of tax. There is a restriction, depending on the enabled tax groups (command 83).
 	Price	This is a singular price that consists of 8 meaningful digits.
 	Quan	A non-mandatory parameter setting the quantity of items for sale. By default, this is 1.000.The length of this parameter is 
			8 meaningful digits (not more than 3 after the decimal point). The result Price*Quan is rounded up to the set number of digits 
			and cannot be longer than 8 meaningful digits.
 	UN		Unit name. A optional text up to 8 characters, describing the unit of the quantity, for example "kg".
 	Perc	This is a non-mandatory parameter which sets the value of discount or surcharge (depending on the sign) in percent over the currently 
			performed sale. Possible values are between - 99.00% and 99.00%, where up to 2 decimal places are acceptable. 
		*/
		
		mFMP.cmd49v0b1(L1, L2, TaxCd, Price, Quan, UN, Perc);
	}
	

	public  void registerSaleAbs(String L1, String L2, String TaxCd, String Price, String Quan, String UN, String AbsSum) throws  IllegalArgumentException, IOException	
	{

	//cmd49v0b2(String L1, String L2, String TaxCd, String Price, String Quan, String UN, String AbsSum)
		/*
		 * 
		31H (49) REGISTRATION OF SALES 

		Parameters
		L1			A text of up to 36 bytes containing one line of description of sale.
 		L2			A text of up to 36 bytes containing a second line describing the sale.
 		TaxCd		One byte containing letter, which indicates the type of tax. There is a restriction, depending on the enabled tax groups (command 83).
 		Price		This is a singular price that consists of 8 meaningful digits.
 		Quan		A non-mandatory parameter setting the quantity of items for sale. By default, this is 1.000.The length of this parameter is 
					8 meaningful digits (not more than 3 after the decimal point). The result Price*Quan is rounded up to the set number of digits 
					and cannot be longer than 8 meaningful digits.
 		UN			Unit name. A optional text up to 8 characters, describing the unit of the quantity, for example "kg".
 		AbsSum		This is a non-mandatory parameter which sets the value of discount or surcharge (depending on the sign) over the currently performed sale. 
					Up to 8 significant digits. Only one of the parameters Perc and Abs allowed.cceptable. 
		 */
		mFMP.cmd49v0b2(L1, L2, TaxCd, Price, Quan, UN, AbsSum);
	}

	/******************************************************/
	/******Register SALE and Show it on screen*************/
	/******************************************************/
	
	/******************************************************/
	public  void calcSubTotalPerc(String ToPrint, String Perc) throws  IllegalArgumentException, IOException
	{
		
	/*
	 * public FiscalResponse  cmd51v0b2 (String ToPrint, String AbsSum) 
 		33h (51) SUBTOTAL 
		Parameters
		ToPrint		One byte, which if '1' the sum of the subtotal will be printed out.
 		AbsSum		A non-mandatory parameter, which shows the value of discount as absolute value (up to 8 digits). 
	 */
		
		mFMP.cmd51v0b1(ToPrint, Perc);
	}

	
	public  void calcSubTotalAbs(String ToPrint, String AbsSum) throws  IllegalArgumentException, IOException
	{
		
	/*
	 * public FiscalResponse  cmd51v0b2 (String ToPrint, String AbsSum) 
 		33h (51) SUBTOTAL 
		Parameters
		ToPrint		One byte, which if '1' the sum of the subtotal will be printed out.
 		AbsSum		A non-mandatory parameter, which shows the value of discount as absolute value (up to 8 digits). 
	 */
		
		mFMP.cmd51v0b2(ToPrint, AbsSum);
	}
	/******************************************************/

	//Calculate TOTAL
	public  void calcSumTotalPaidAmnt(String PaidMode, String Amount_In) throws  IllegalArgumentException, IOException
	{
		/*
	 		35H (53) CALCULATION OF A TOTAL 
	 		cmd53v0b0(String PaidMode, String Amount_In) 
			Parameters	
			�P� Payment in cash
			�N� Payment via credit
			�C� Payment in cheques
			Amount_In	The sum tendered (up to 9 meaningful symbols) 
		 */
	
		mFMP.cmd53v0b0(PaidMode, Amount_In);
	}
	
	public  void calcSumTotalNone() throws  IllegalArgumentException, IOException
	{
		/*
	 		35H (53) CALCULATION OF A TOTAL 
		 */
	
		mFMP.cmd53v0b1();
	}

	public  void calcSumTotalPaidMode(String PaidMode) throws  IllegalArgumentException, IOException
	{
		/*
	 		35H (53) CALCULATION OF A TOTAL 
	 		cmd53v0b0(String PaidMode, String Amount_In) 
			Parameters	
			Amount_In	The sum tendered (up to 9 meaningful symbols) 
		 */
	
		mFMP.cmd53v0b2(PaidMode);
	}

	public  void calcSumTotalAmntIn(String Amount_In) throws  IllegalArgumentException, IOException
	{
		/*
	 		35H (53) CALCULATION OF A TOTAL 
	 		cmd53v0b0(String PaidMode, String Amount_In) 
			Parameters	
			Amount_In	The sum tendered (up to 9 meaningful symbols) 
		 */
	
		mFMP.cmd53v0b3(Amount_In);
	}

	/******************************************************/

	
	//Print free fiscal text
	public  void printFreeFiscalText(String Input_Text) throws  IllegalArgumentException, IOException
	{
		/*
		 36H (54) PRINTING A FREE FISCAL TEXT 
		Parameters
			Input_Text	Up to 42 bytes text
		*/
		
		mFMP.cmd54v0b0 (Input_Text);
	}	 
	
	
	/******************************************************/
	
	//Close fiscal receipt
	public  void closingFiscalReceipt() throws  IllegalArgumentException, IOException
	{
		mFMP.cmd56v0b0();
	}	

	/******************************************************/

	public  void registerItemForSalePerc(String PLU, String Quan, String UN, String Perc) throws  IllegalArgumentException, IOException
	{
		//cmd58v0b0(String PLU, String Quan, String UN, String Perc);
		/*
	PLU		The individual number of the item - a whole number between 1 and 999999999 (not more than 9 digits).
 	Quan	Parameter setting the quantity of the items for sale with a default value of 1.000. Length cannot be longer than 8 meaningful 
			digits (not more than 3 after the decimal point). The resulting singular price (*Quan) is rounded up to the set number of digits 
			after the decimal point and also cannot be greater than 8 meaningful digits.
 	
 	UN		Unit name. A optional text up to 8 characters, describing the unit of the quantity, for example "kg".
 	Perc	Parameter showing the value of surcharge or discount (depending on the symbol) in percent over the current sale. Possible 
			values are between -99.00% to 99.00%. Up to 2 digits after the decimal point are acceptable.
		 */
		mFMP.cmd58v0b0(PLU, Quan, UN, Perc);
	}	


	public  void registerItemForSaleAbsSum(String PLU, String Quan, String UN, String AbsSum) throws  IllegalArgumentException, IOException
	{
		//cmd58v0b0(String PLU, String Quan, String UN, String Perc);
		/*
	PLU		The individual number of the item - a whole number between 1 and 999999999 (not more than 9 digits).
 	Quan	Parameter setting the quantity of the items for sale with a default value of 1.000. Length cannot be longer than 8 meaningful 
			digits (not more than 3 after the decimal point). The resulting singular price (*Quan) is rounded up to the set number of digits 
			after the decimal point and also cannot be greater than 8 meaningful digits.
 	
 	UN		Unit name. A optional text up to 8 characters, describing the unit of the quantity, for example "kg".
 	AbsSum	Parameter which sets the value of discount or surcharge (depending on the sign) over the current sale. Up to 8 significant digits. 
		 */
		mFMP.cmd58v0b1(PLU, Quan, UN, AbsSum);
	}	
	
	/******************************************************/
	
	//Cancel Fiscal receipt
	public  void cancelFiscalReceipt() throws  IllegalArgumentException, IOException
	{
		mFMP.cmd60v0b0();
	}	

	/******************************************************/

	/*
	BC_Type	Bar code type. 1 byte with possible value:
	
		'1' EAN8 bar code. Data contains only digits and is 7 bytes long. The check sum is automatically calculated and printed.
		'2' EAN13 bar code. Data contains only digits and is 12 bytes long. The check sum is automatically calculated and printed.
		'3' Code128 bar code. Data contains symbols with ASCII codes between 32 and 127. Data length is between 16 and 32 symbols
			(depends on the content - the maximum length is if all symbol are digits). The check sum is automatically calculated and printed.
			
		'4' Interleaved 2 of 5 bar code. Data contains only digits and is up to 28 bytes long. No check sum is calculated and printed.
		'5' Interleaved 2 of 5 bar code. Data contains only digits and is up to 27 bytes long. The check sum is automatically calculated and printed.

	Data	- EAN8 bar code. Data contains only digits and is 7 bytes long. The check sum is automatically calculated and printed.
	
		- EAN13 bar code. Data contains only digits and is 12 bytes long. The check sum is automatically calculated and printed.
		- Code128 bar code. Data contains symbols with ASCII codes between 32 and 127. Data length is between 16 and 32 symbols
			(depends on the content - the maximum length is if all symbol are digits). The check sum is automatically calculated and printed.
			
		- Interleaved 2 of 5 bar code. Data contains only digits and is up to 28 bytes long. No check sum is calculated and printed.
		- Interleaved 2 of 5 bar code. Data contains only digits and is up to 27 bytes long. The check sum is automatically calculated	and printed.
	 */
	public  void printBarcodeTypeWithoutNumber(String BC_Type, String Data) throws  IllegalArgumentException, IOException
	{
		//mFMP.cmd84v0b0(BC_Type, Data);
		String bcData = BC_Type + ";" + Data;
		//System.out.println("WithOutNUmber" + bcData);
		mFMP.customCommand(84, bcData);
	}
	
	
	public  void printBarcodeTypeWithNumber(String BC_Type, String Data) throws  IllegalArgumentException, IOException
	{
		//		mFMP.cmd84v0b1(BC_Type, Data);
		String bcData = BC_Type + "," + Data;
		//System.out.println("WithNUmber" + bcData);
		mFMP.customCommand(84, bcData);		
	}

	/******************************************************/

	public  void dailyXClosure() throws  IllegalArgumentException, IOException
	{
		//Option 
    	//0 -  Z
    	//2 -  X
		
		/* 
		 * N The presence of this symbol cancels the option to clear the data accumulated on the operators during a Z-report.
		 * A The presence of this symbol cancels the option to clear the data about sold article quantities during a Z-report
		 */ 

		mFMP.customCommand(69, "2");
		//mFMP.cmd69v0b0 ("2", "N", "A"); 
	}

	public  void dailyZClosureNoClearAccOPs() throws  IllegalArgumentException, IOException
	{
		//Option 
    	//0 -  Z
    	//2 -  X
		
		/* 
		 * N The presence of this symbol cancels the option to clear the data accumulated on the operators during a Z-report.
		 * A The presence of this symbol cancels the option to clear the data about sold article quantities during a Z-report
		 */ 

		mFMP.customCommand(69, "0N");
		//mFMP.cmd69v0b0 ("0", "N", "0"); 
	}

	public  void dailyZClosureNoClearSold() throws  IllegalArgumentException, IOException
	{
		//Option 
    	//0 -  Z
    	//2 -  X
		
		/* 
		 * N The presence of this symbol cancels the option to clear the data accumulated on the operators during a Z-report.
		 * A The presence of this symbol cancels the option to clear the data about sold article quantities during a Z-report
		 */ 

		//mFMP.cmd69v0b0 ("0", "0", "A");
		mFMP.customCommand(69, "0A");
	}

	
	/******************************************************/
	/***************Electronic Journal need to add*********/
	/******************************************************/

	public  void paperAdvance(String TRGT_LINES) throws  IllegalArgumentException, IOException
	{
		/*
		 * 
		2Ch (44) ADVANCING PAPER 
		Parameters
		TRGT_LINES	Advancing paper measured in text lines. The programmed line count cannot be greater than 99 (1 or 2 bytes). 
		 */
		
		mFMP.cmd44v0b0 (TRGT_LINES) ;
	}

	/*******************************************/
	/********Paper cutoff is automatic..********/
	/*******************************************/
	
	/******************************************************/

	public  void printSeperatorLines(String LineType) throws  IllegalArgumentException, IOException
	{
		/*
		 * LineType	The type of the separator line. One byte with possible value: 
			'1' The line is filled with the symbol '-'. 
			'2' The line is filled with the symbols '-' and ' ' (space). 
			'3' The line is filled with the symbols '='. 
		 */
		mFMP.cmd92v0b0(LineType);
	}	
	
	/******************************************************/
	
	//Print diag information
	public  void printDiagInfo() throws  IllegalArgumentException, IOException
	{
		mFMP.cmd71v0b0();
	}	

	/*********************/
	/*
	 * Drawer kickout need to add
	 */
	public  void drawerKickOut(String delayTime) throws  IllegalArgumentException, IOException
	{
		/*

	ITEM_INDEX
		One symbol having the following meaning: 

	"0".."5" Selects the THE HEADER (any one of numbers will record the whole Header). 
	"6" or "7" Selects the first or second FOOTER (TAX OFFICE) line. 
	"B" Set bar code height in pixels (0.125 mm). Possible values from 24 (3 mm) to 240 (30 mm). The barcode is printed with 
	command 84 (54H). 
	"C" Permission/rejection of the automatic cutting of paper after each receipt. After switching ON, the performance of printer 
	is defined in accordance with the setting of the switch SW1. 
	"D" Set print darkness. Possible values: 
	'1': Very low 
	'2': Low 
	'3': Normal 
	'4': Dark 
	'5': Very dark 
	"L" Height of graphic logo and permission/rejection of the printing of graphic logo immediately before the header. This logo is 
	defined with command 115 (73H). 
	"X" Enable / disable automatic cash drawer pulse in commands 53 (35H) and 70 (46H). 
	"I" Gives us the option to read values, set earlier with command 43. After the letter "I" only one more symbol follows which 
	coincides with some of the above.
 
	DATA_VALUE
		A text string: 

	If  is '0'..'5' - 

	LineX The HEADER line, which is defined. Up to 48 bytes, but the total data length must be no more than 218 symbols. Only the needed line count must be set (If the name and address did not require 6 lines). 
	Tab Tabulation (ASCII code 09h). 
	The whole text of HEADER which will be writed in Fiscal Memory at once!!! Before the fiscalisation the header is not written to the fiscal memory and may be changed unlimited times. Command 72 (Fiscalisation) writes the first names record with the currently programmed header lines. After the fiscalisation 9 changes are allowed. 
	If  is '6' or '7' - the text of the first or second footer line (up to 48 symbols). 
	If  = 'B' - A number - the height of bar code in pixels. 
	If  = 'C' - One symbol value '0' or '1', where "0" forbids and "1" permits the automatic cutting of the receipt. 
	If  = 'D' - The print darkness (1 to 5). 
	If  = `L' Syntax , 
	Enabled '0' or '1', where '1' means, that logo printing is enabled. 
	Height Graphics logo height in lines (0.125 mm). A number from 8 to 96. 
	If  = 'X' - One symbol: '0' or '1', where '1' disables and '0' enables automatic cash drawer pulse in commands 53 (35H) and 70 (46H). 
	 */
		
		//mFMP.cmd43v0b0 ("X", enable);
		mFMP.customCommand(106, delayTime);
	}
	/*********************/

	public  void graphicLogoPrint(String enable) throws  IllegalArgumentException, IOException
	{
		/*
		 * 	"L" Height of graphic logo and permission/rejection of the printing of graphic logo immediately before the header. This logo is 
			defined with command 115 (73H). 

			If  = `L' Syntax , 
			Enabled '0' or '1', where '1' means, that logo printing is enabled. 
		 */
		mFMP.cmd43v0b0("L", enable);
	}

	public  void autoPaperCut(String enable) throws  IllegalArgumentException, IOException
	{
		/*
		 * 	"C" Permission/rejection of the automatic cutting of paper after each receipt. After switching ON, the performance of printer 
			is defined in accordance with the setting of the switch SW1. 

			If  = 'C' - One symbol value '0' or '1', where "0" forbids and "1" permits the automatic cutting of the receipt. 

		 */
		mFMP.cmd43v0b0("C", enable);
	}

	public  void barcodHeight(String height) throws  IllegalArgumentException, IOException
	{
		/*
		 * 	"B" Set bar code height in pixels (0.125 mm). Possible values from 24 (3 mm) to 240 (30 mm). The barcode is printed with 
				command 84 (54H). 
			If  = 'B' - A number - the height of bar code in pixels. 

		 */
				
		mFMP.cmd43v0b0("B", height);
	}
	
}


