package POS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.datecs.fiscalprinter.tza.FMP10TZA;
import com.fazecast.jSerialComm.*;
import org.mindrot.jbcrypt.*;

import java.sql.*;

public class UI{
	private List<String> getCustomerSuggestions(String input) {
	    List<String> suggestions = new ArrayList<>();
	    String dbUrl = "jdbc:sqlserver://localhost:1433;databaseName=aroniumdb";
	    String username = "sa";
	    String password = "password1@";

	    String query = "SELECT Name FROM Customer WHERE Name LIKE ?"; // Replace 'Customers' with your table name
	    try (Connection conn = DriverManager.getConnection(dbUrl, username, password);
	         PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setString(1, input + "%"); // Fetch names starting with the input
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            suggestions.add(rs.getString("Name"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return suggestions;
	}
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
	JButton btnTransactions, btnCustomerSearch, btnSelect, btnSaveOrder, btnVoid, btnSearch, btnPrintRcpt, btnLastTrans, btnlogoff, btnCustomerSelect, btnRcptReprint;
    DefaultTableModel model, resultModel, CustomerResultModel;
    JTable transactionsTable, tblSalesTable, tblSearchResult, tblCustomerSearchResult;
    JTextField txtCustomerName, txtSearch, txtBcode, txtTamount, txtChange, txtTotal, txtOrderNumber, txtTableNumber, txtDiscount, txtCustomerSearch;
    JLabel lblBcode, lbldepartment, lblTamount, lblChange, lblTotal, lblCustomer, lblSection, lblTableNumber, lblUsernameTxt, lblSessionIDTxt;
    JComboBox<String> cmbSection, cmbdepartment, cmbTableNumber, CmbCustomerName;
    JPanel searchPanel, CustomerSearchPanel;
    JDialog searchDialog, CustomerSearchDialog;
    JScrollPane searchScrollPane, CustomerSearchScrollPane;
    public static FMP10TZA mFMP;
    public static InputStream inputBuffer;
    public static OutputStream outputBuffer;
    public static SerialPort sp;
    static UI fp = null;
    double tendered = 0.0;
    double changing = 0.0;
    Font f = new Font("Arial", Font.BOLD, 32);
    Font sf = new Font("Arial", Font.PLAIN, 20);
    public static final char ESC = 27;
    public static final String Bold_on = ESC + "E";
	public static final String Bold_off = ESC + "F";
	static String userId = "";
    static String sessionId = "";
    JComboBox<String> cbPayment;
    
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

        btnRcptReprint = new JButton("Reprint");
        btnRcptReprint.addActionListener(act);
        btnRcptReprint.setBackground(new Color(52, 235, 140));
        btnRcptReprint.setForeground(Color.BLACK);
        btnRcptReprint.setFont(sf);
        buttonPanel.add(btnRcptReprint);

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
        
     // Step 1: Create the "Transactions" button
        btnTransactions = new JButton("Transactions");
        btnTransactions.setBackground(new Color(204, 220, 237)); // RGB for #65acad
        btnTransactions.setFont(sf); // Assuming 'sf' is your predefined font
        btnTransactions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTransactionDialog();
            }
        });
        buttonPanel.add(btnTransactions);

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
        JLabel lblCustomerName = new JLabel("Client Name");
        lblCustomerName.setFont(sf);
        lblCustomerName.setForeground(Color.WHITE);
        rightGbc.gridx = 0;  // Align label to the left side
        rightGbc.gridy = 0;  // Position in the first row
        rightPanel.add(lblCustomerName, rightGbc);

        // Customer Name Text Field (inside right panel)
        txtCustomerName = new JTextField();
        txtCustomerName.setFont(f);
        txtCustomerName.setPreferredSize(new Dimension(400, 30));
        //txtCustomerName.setEditable(true);
        rightGbc.gridx = 1;  // Align text field to the right side of the label
        rightPanel.add(txtCustomerName, rightGbc);
        txtCustomerName.setEditable(false);
        
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
        
     // Adding the Discount Button to the right panel
        btnCustomerSearch = new JButton("Customer Search");
        btnCustomerSearch.setFont(sf);
        btnCustomerSearch.addActionListener(act);
        rightGbc.gridx = 1; // Align the button to the left side
        rightGbc.gridy = 2;  // Position below the Payment field
        rightGbc.gridwidth = 1; // Span across both columns
        rightPanel.add(btnCustomerSearch, rightGbc);
        btnCustomerSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createCustomerSearchDialog();
            }
        });

        rightPanel.add(btnCustomerSearch, rightGbc);
        
        gbc.gridx = 2; // Position to the right of txtBcode and txtTamount
        gbc.gridy = 4; // Align with the row of txtBcode
        gbc.gridheight = 3; // Span the rows of txtBcode and txtTamount
        gbc.fill = GridBagConstraints.BOTH; // Allow expansion
        gbc.weightx = 0.5; // Adjust as needed
        gbc.weighty = 0.0; // Adjust as needed
        gbc.insets = new Insets(5, 5, 5, 5); // Add padding if necessary

        frame.add(rightPanel, gbc);
        
        frame.setVisible(true);
        btnPrintRcpt.setEnabled(false);
        //WinFocusListener winFocus = new WinFocusListener();
        //frame.addWindowListener(winFocus);
        
        createSearchDialog();
        //getsessionId();
	}
	
	public UI(String string) {
		// TODO Auto-generated constructor stub
		
	}
	

    public void showTransactionDialog() {
        // Create the dialog
        JDialog dialog = new JDialog(frame, "User Transactions", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout());

        // Create the JComboBox for payment types
        final JComboBox<String> paymentTypeComboBox = new JComboBox<>();
        populatePaymentTypes(paymentTypeComboBox);

        // Create the JTable for transactions
        String[] columnNames = {"Number", "Total"};
        final DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        transactionsTable = new JTable(tableModel);

        // Add action listener to JComboBox to load transactions on selection
        paymentTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedPaymentType = (String) paymentTypeComboBox.getSelectedItem();
                loadTransactions(selectedPaymentType, tableModel);
            }
        });

        // Add components to the dialog
        dialog.add(paymentTypeComboBox, BorderLayout.NORTH);
        dialog.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

        // Display the dialog
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    // Populate payment types into the JComboBox
 // Populate payment types into the JComboBox using the getConnection() method
    private void populatePaymentTypes(JComboBox<String> comboBox) {
        try (Connection conn = getConnection()) { // Get database connection
            if (conn == null) return; // Exit if connection fails

            String query = "SELECT name FROM PaymentType ORDER BY name";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                comboBox.addItem("All"); // Default option
                while (rs.next()) {
                    comboBox.addItem(rs.getString("name"));
                }
            }
        } catch (SQLException ex) {
            logError(ex.getMessage(), new File("C:\\libraries\\logs\\errorLog"));
            ex.printStackTrace();
        }
    }

 // Load transactions based on selected payment type
    private void loadTransactions(String paymentType, DefaultTableModel tableModel) {
        // Clear existing data
        tableModel.setRowCount(0);

        String query;
        if ("All".equals(paymentType)) {
            query = "SELECT d.Number, d.Total FROM Document d " +
                    "INNER JOIN Payment p ON d.Id = p.DocumentId " +
                    "WHERE d.Date = CAST(GETDATE() AS DATE)";
        } else {
            query = "SELECT d.Number, d.Total FROM Document d " +
                    "INNER JOIN Payment p ON d.Id = p.DocumentId " +
                    "INNER JOIN PaymentType pt ON p.PaymentTypeId = pt.Id " +
                    "WHERE pt.Name = ? AND d.Date = CAST(GETDATE() AS DATE)";
        }

        try (Connection conn = getConnection();  // Get database connection
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            if (!"All".equals(paymentType)) {
                pstmt.setString(1, paymentType);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String number = rs.getString("number");
                    double total = rs.getDouble("total");
                    tableModel.addRow(new Object[]{number, total});
                }
            }
        } catch (SQLException ex) {
            logError(ex.getMessage(), new File("C:\\libraries\\logs\\errorLog"));
            ex.printStackTrace();
        }
    }

	/*private void showTransactionsDialog() {
	    // Create the dialog
	    JDialog dialog = new JDialog(frame, "User Transactions", true);
	    dialog.setSize(400, 300);
	    dialog.setLayout(new BorderLayout());

	    // Create the JComboBox for payment types
	    JComboBox<String> paymentTypeComboBox = new JComboBox<>();
	    populatePaymentTypes(paymentTypeComboBox);

	    // Create the JTable for transactions
	    String[] columnNames = {"Number", "Total"};
	    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
	    JTable transactionsTable = new JTable(tableModel);

	    // Add action listener to JComboBox to load transactions on selection
	    paymentTypeComboBox.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            /*String selectedPaymentType = (String) paymentTypeComboBox.getSelectedItem();
	            loadTransactions(selectedPaymentType, tableModel);*/
	        //}
	    /*});

	    // Add components to the dialog
	    dialog.add(paymentTypeComboBox, BorderLayout.NORTH);
	    dialog.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);

	    // Display the dialog
	    dialog.setLocationRelativeTo(frame);
	    dialog.setVisible(true);
	}

	// Step 3: Method to populate payment types into the JComboBox
	private void populatePaymentTypes(JComboBox<String> comboBox) {
	    String query = "SELECT name FROM paymentype";
	    /*try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        while (rs.next()) {
	            comboBox.addItem(rs.getString("name"));
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }*/
	//}

	// Step 4: Method to load transactions based on selected payment type
	/*private void loadTransactions(String paymentType, DefaultTableModel tableModel) {
	    // Clear existing data
	    tableModel.setRowCount(0);

	    String query = "SELECT number, total FROM document WHERE paymentype_id = " +
	                   "(SELECT id FROM paymentype WHERE name = ?) AND date = CAST(GETDATE() AS DATE)";
	    /*try (Connection conn = DriverManager.getConnection(ar, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, paymentType);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            while (rs.next()) {
	                String number = rs.getString("number");
	                double total = rs.getDouble("total");
	                tableModel.addRow(new Object[]{number, total});
	            }
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }*/
	//}
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
	
	//Patients Searching dialog box
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
	 
	 private class autoCustomerSearch implements DocumentListener {
		    public void insertUpdate(DocumentEvent e) { searchCustomers(txtCustomerSearch.getText()); }
		    public void removeUpdate(DocumentEvent e) { searchCustomers(txtCustomerSearch.getText()); }
		    public void changedUpdate(DocumentEvent e) { searchCustomers(txtCustomerSearch.getText()); }
	}
	 
	 private void logError(String errorMessage, File logFile) {
		    try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
		        logWriter.write(new java.util.Date() + " - ERROR: " + errorMessage);
		        logWriter.newLine();
		    } catch (IOException ex) {
		        ex.printStackTrace();
		    }
		}

	 private Connection getConnection() {
		    File configFile = new File("C:\\libraries\\config.txt"); // Path to config file
		    File posErrorLog = new File("C:\\libraries\\logs\\errorLog"); // Path to log errors

		    try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
		        // Read config values
		        String comPort = reader.readLine();  // Not needed for DB, but kept
		        String ip = reader.readLine();       // Database IP
		        String user = reader.readLine();     // DB username
		        String pass = reader.readLine();     // DB password
		        String dbName = reader.readLine();   // Database name
		        String till = reader.readLine();     // Till info (optional)

		        // JDBC connection string using the extracted values
		        String url = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + dbName + ";encrypt=true;trustServerCertificate=true";

		        return DriverManager.getConnection(url, user, pass); // Return the DB connection

		    } catch (IOException | SQLException e) {
		        logError(e.getMessage(), posErrorLog); // Log errors if any issue
		        e.printStackTrace();
		        return null; // Return null if connection fails
		    }
		}

	 	private void searchCustomers(String keyword) {
		    try (Connection conn = getConnection()) { // Get DB connection
		        if (conn == null) return; // If connection failed, exit
		      
		     // Clear table properly
		        while (CustomerResultModel.getRowCount() > 0) {
		            CustomerResultModel.removeRow(0);
		        }
		        String query = "SELECT Id, Name, PhoneNumber FROM Customer WHERE Name LIKE ?";
		        System.out.println(query);
		        PreparedStatement pst = conn.prepareStatement(query);
		        pst.setString(1, "%" + keyword + "%");
		        System.out.println(keyword);
		        ResultSet rs = pst.executeQuery();
		        
		        while (rs.next()) {
		            CustomerResultModel.addRow(new Object[]{rs.getInt("Id"), rs.getString("Name"), rs.getString("PhoneNumber")});
		        }
		    } catch (SQLException ex) {
		        logError(ex.getMessage(), new File("C:\\libraries\\logs\\errorLog"));
		        ex.printStackTrace();
		    }
		}

	
	 private class SelectAction implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	        int row = tblCustomerSearchResult.getSelectedRow();
	        if (row != -1) {
	        	String customerName	= CustomerResultModel.getValueAt(row, 0).toString();
	            customerName += " - ";
	            customerName += CustomerResultModel.getValueAt(row, 1).toString();
	            txtCustomerName.setText(customerName); // Set customer name in the main POS frame
	            CustomerSearchDialog.dispose();
	        }
	    }
	}

	 
	 private void createCustomerSearchDialog() {
		    CustomerSearchDialog = new JDialog(frame, "Search Customer", true);
		    CustomerSearchDialog.setSize(800, 600);
		    CustomerSearchDialog.setLayout(new BorderLayout());

		    // Search Panel
		    CustomerSearchPanel = new JPanel(new BorderLayout());
		    txtCustomerSearch = new JTextField();
		    txtCustomerSearch.setFont(sf);
		    txtCustomerSearch.getDocument().addDocumentListener(new autoCustomerSearch());
		    CustomerSearchPanel.add(txtCustomerSearch, BorderLayout.NORTH);

		    // Table Model for Customers
		    CustomerResultModel = new DefaultTableModel();
		    CustomerResultModel.addColumn("ID");
		    CustomerResultModel.addColumn("Name");
		    CustomerResultModel.addColumn("Contact");
		    tblCustomerSearchResult = new JTable(CustomerResultModel);
		    tblCustomerSearchResult.setFont(sf);
		    CustomerSearchScrollPane = new JScrollPane(tblCustomerSearchResult);
		    CustomerSearchPanel.add(CustomerSearchScrollPane, BorderLayout.CENTER);

		    // Select Button
		    SelectAction selectActCustomer = new SelectAction();
		    btnCustomerSelect = new JButton("Select");
		    btnCustomerSelect.setFont(sf);
		    btnCustomerSelect.addActionListener(selectActCustomer);
		    CustomerSearchPanel.add(btnCustomerSelect, BorderLayout.SOUTH);

		    CustomerSearchDialog.add(CustomerSearchPanel);
		    
		    CustomerSearchDialog.setVisible(true);
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
		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getKeyCode()==KeyEvent.VK_F5){
				JOptionPane.showMessageDialog(null, "Test");
			}
			if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
				try {
					File file = new File("C:\\libraries\\config.txt");
					File posErrorLog = new File("C:\\libraries\\logs\\errorLog");
					@SuppressWarnings("resource")
					BufferedWriter LogWriter = new BufferedWriter(new FileWriter(posErrorLog));
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					String till = reader.readLine();
					LocalDate mydate = LocalDate.now();
					String countOrders = "SELECT COUNT(dbo.Document.DocumentTypeId) as \"rows\" from Document where DocumentTypeId = 2";
					LogWriter.append(countOrders);
					int numOrders = 0;
					String total = txtTotal.getText();
					String paddedOrders = "";
					LocalDateTime dateTime = LocalDateTime.now();
					DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String formatedDate = dateTime.format(ObjdateFormated);
					String paymentType = cbPayment.getSelectedItem().toString(); // Retrieve payment type from UI
					String paymentAmount = txtTotal.getText();      // Total amount
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
			        	System.out.println(lblUsernameTxt.getText());
			        	String userName = lblUsernameTxt.getText();
			        	
			        	String input = "";
			        	input = txtCustomerName.getText();
			        	// Split the string by " - "
			        	String customerId = "";
			        	if(!input.isEmpty()){
			        		String[] parts = input.split(" - ", 2);
				            // Extract values
				            customerId = parts[0];  // "Id"
				            String customerName = parts[1];    // "Customer Name"
				            // Print results
				            System.out.println("Number: " + customerId);
				            System.out.println("Text: " + customerName);
			        	}
			            			        	
			            
			            
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
			            String transactSQL = "insert into [aroniumdb].[dbo].[document] (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule, SessionId) values ('19-200-"+paddedOrders +"',(SELECT Id FROM Users WHERE Username = '" + userName + "'), '" + customerId + "', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0," + sessionId3 + ")";
			        	System.out.println(transactSQL);
			        	stmtSt.executeUpdate(transactSQL, Statement.RETURN_GENERATED_KEYS);
			        	ResultSet generatedKeys = stmtSt.getGeneratedKeys();
			        	int documentId = 0;
			        	if (generatedKeys.next()) {
			        	    documentId = generatedKeys.getInt(1);  // Get the auto-generated documentId
			        	    System.out.println(documentId);
			        	}
			        	
			        	String paymentSQL = "INSERT INTO [aroniumdb].[dbo].[Payment] (documentId, paymentTypeId, Amount, Date) VALUES (" + documentId + ", (SELECT Id FROM PaymentType WHERE Name = '" + paymentType + "'), " + paymentAmount + ",'" + mydate + "')";
			        	System.out.println(paymentSQL);
			        	stmtSt.executeUpdate(paymentSQL);  // Insert payment record
			        	
			        	for (int count = 0; count < model.getRowCount(); count++){
					           String id = (model.getValueAt(count, 0).toString());
					           double QtySold = Double.parseDouble(model.getValueAt(count, 3).toString());
					           double salesTax = 0.0;
					           double priceAfterTax = Double.parseDouble(model.getValueAt(count, 4).toString());
					           double itemTotalAfterTax = Double.parseDouble(model.getValueAt(count, 5).toString());
					           double priceBeforeTax = 0.0;
					           double totalPriceBeforeTax = 0.0;
					           String tax = (model.getValueAt(count, 2).toString());
					           if (tax.equals("B")){
					        	   salesTax = priceAfterTax * 0.165;
					        	   priceBeforeTax = priceAfterTax - salesTax;
					        	   //itemTotalAfterTax = (itemTotalAfterTax - (salesTax * QtySold));
					           }
					           else{
					        	   priceBeforeTax = priceAfterTax;
					        	   itemTotalAfterTax = itemTotalAfterTax;
					           }
					           String upDateDocItem = "insert into [aroniumdb].[dbo].[DocumentItem](DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
					           		" ((select id from aroniumdb.dbo.Document where OrderNumber ='" + numOrders + "'), " +  id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0)";
					           System.out.println(upDateDocItem);
					           stmtSt.executeUpdate(upDateDocItem);
					           System.out.println(numOrders);
			        	}	   
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
					for (int count = 0; count < model.getRowCount(); count++){
				           String id = (model.getValueAt(count, 0).toString());
				           double sale = Double.parseDouble(model.getValueAt(count, 3).toString());
				           String SQL = "SELECT * FROM [aroniumdb].[dbo].[stock] where productId = " +  id;
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
								        	String updateSQL = "update [aroniumdb].[dbo].[stock] set Quantity = " + balance + "where ProductId = " +  id;
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
					String Tandered = txtTamount.getText();
					String Pmode = "P";
					PrinterService printerServices = new PrinterService();
					byte[] cutP = new byte[] { 0x1d, 'V', 1 };
					Double DBsubTotal = 0.0;
					Double Total = 0.0;
					Double ItemTax = 0.0;
					char taxCode = 'A';
			        String taxCode2 = String.valueOf(taxCode);
			        Double taxTotal = 0.0;
			        Double taxableSales = 0.0;
			        Double nontaxableSales = 0.0;
			        String printText = "";
			        LocalTime now = LocalTime.now();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
					System.out.println(now.format(formatter));
					// Load and print the logo
			        String logoPath = "C:\\Libraries\\company_logo.png"; // Your logo path
			        byte[] logoBytes = ImagePrinter.convertImageToESC_POS(logoPath);
			        printerServices.printBytes("POS-Printer", logoBytes); // Print the logo
					printerServices.printString("POS-Printer", Bold_on + " GIFTED HANDS PVT CLINIC" + Bold_off + "\n\n");
		            printerServices.printString("POS-Printer", "Lilongwe \n");
		            printerServices.printString("POS-Printer", "Our Confidence Is Our Capabality \n");
		            printerServices.printString("POS-Printer", "Cell: +265 886 498 222  \n     +265 995 767 137 \n");
		            printerServices.printString("POS-Printer", "Date: " + mydate + "\n\n");
					printerServices.printString("POS-Printer", "Time: " + now.format(formatter) + "\n\n");
					printerServices.printString("POS-Printer", "Transaction No.: 19-200-"+paddedOrders);
					int strSize = 0;
					printerServices.printString("POS-Printer", "\n------------------------------------------\n");
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
				           total = total + DBsubTotal;
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
				           printText +=  desc + "  " + price + " X " + q + "  " + DBsubTotal +"  "+ tax +"\n";
				           //printerServices.printString("EPSON TM-T20II Receipt", desc + "  " + price + " X " + q + "  " + DBsubTotal +"  "+ tax +"\n");
				           //sellItem(desc, tax, price, q);
					}
					printerServices.printString("POS-Printer", printText);
					printerServices.printString("POS-Printer", "------------------------------------------\n");
					BigDecimal sbCash = new BigDecimal(txtTamount.getText());
					sbCash = sbCash.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbTaxable = new BigDecimal(taxableSales);
					sbTaxable = sbTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbNonTaxable = new BigDecimal(nontaxableSales);
					sbNonTaxable = sbNonTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbItemTax = new BigDecimal(ItemTax);
					sbItemTax = sbItemTax.setScale(2, RoundingMode.HALF_UP);
					String salesDetails = "";
					/*printerServices.printString("EPSON TM-T20II Receipt", "Sales Taxable A    : " + sbTaxable +"\n");
					printerServices.printString("EPSON TM-T20II Receipt", "Tax Total          : " + sbItemTax +"\n");
					printerServices.printString("EPSON TM-T20II Receipt", "Sales NonTaxable B : " + sbNonTaxable +"\n");
					printerServices.printString("EPSON TM-T20II Receipt", "Total              : " + txtTotal.getText() +"\n");
					printerServices.printString("EPSON TM-T20II Receipt", "Cash               : " + sbCash +"\n");
					printerServices.printString("EPSON TM-T20II Receipt", "Change             : " + txtChange.getText() +"\n");
					printerServices.printString("EPSON TM-T20II Receipt", "------------------------------------------\n");*/
					salesDetails += "Sales Taxable A    : " + sbTaxable +"\n" + "Tax Total          : " + sbItemTax +"\n" + "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
					salesDetails += "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
					printerServices.printString("POS-Printer", salesDetails);
					//calcSumTotalPaidAmnt(Pmode, Tandered);
					//totalCash();
					printerServices.printString("POS-Printer", "------------------------------------------\n");
					printerServices.printString("POS-Printer", "Operator: " + lblUsernameTxt.getText() + "\n\n");
					printerServices.printString("POS-Printer", "------------------------------------------\n");
					printerServices.printString("POS-Printer", "Get well soon!!!!!\n\n\n\n\n");
					printerServices.printBytes("POS-Printer", cutP);
					//closingFiscalReceipt();
					//disconnect();
					model.setRowCount(0);
					txtTotal.setText("");
					txtChange.setText("");
					txtTamount.setText("");
					txtBcode.grabFocus();
					btnPrintRcpt.setEnabled(false);
					//model.addRow(new Object[] {"Description", "TAX ID", "Quantity", "Price"});
								
				} catch (IllegalArgumentException | IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				txtBcode.grabFocus();
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
			String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass;
			try (Connection con = DriverManager.getConnection(connectionUrl);
		             PreparedStatement pst = con.prepareStatement("SELECT * FROM [aroniumdb].[dbo].[users] WHERE UserName = ? AND Accesslevel = 9")) {
		            pst.setString(1, username);
		            //pst.setString(2, password);
		            ResultSet rs = pst.executeQuery();
                    
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(null, "!!!Wrong User Name or password!!!");
                    } else {
    		            String hashedPassword = rs.getString("Password");
                    	String compatibleHash = hashedPassword.replace("$2y$", "$2a$");
                    	if (BCrypt.checkpw(password, compatibleHash)) {
                    		isValid = true;
                    	}
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
			
			if (e.getSource()==btnSearch){
		        searchDialog.setVisible(true);
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
					System.out.println("test select");
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
				        	String transactSQL = "insert into [aroniumdb].[dbo].[document] (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule, sessionId) values ('19-200-"+paddedOrders +"','1', '1', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0," + sessionId +")";
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
			if (e.getSource()==btnRcptReprint){
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
					//model.setRowCount(0);
					
					PrinterService printerServices = new PrinterService();
					byte[] cutP = new byte[] { 0x1d, 'V', 1 };
					float DBsubTotal = 0;
					Double Total = 0.0;
					Double ItemTax = 0.0;
					char taxCode = 'A';
			        String taxCode2 = String.valueOf(taxCode);
			        Double taxTotal = 0.0;
			        Double taxableSales = 0.0;
			        Double nontaxableSales = 0.0;
			        String printText = "";
			        
			      //Load and print the logo
			        printerServices.printString("POS-Printer", " DUPLICATE \n");
			        String logoPath = "C:\\Libraries\\company_logo.png"; // Your logo path
			        byte[] logoBytes = ImagePrinter.convertImageToESC_POS(logoPath);
			        printerServices.printBytes("POS-Printer", logoBytes); // Print the logo
			        printerServices.printString("POS-Printer", Bold_on + "GIFTED HANDS PVT CLINIC" + Bold_off + "\n\n");
		            printerServices.printString("POS-Printer", "Lilongwe \n");
		            printerServices.printString("POS-Printer", "Our Confidence Is Our Capabality \n");
		            printerServices.printString("POS-Printer", "Cell: +265 886 498 222  \n      +265 995 767 137 \n");
		           
					
					int strSize = 0;
					
			        
					try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT d.Id, d.Number, d.Date, d.Total AS DocumentTotal, di.ProductId, di.Quantity, di.Price, di.Total, p.Name, u.Username FROM [dbo].[Document] AS d, [dbo].[DocumentItem] AS di, [aroniumdb].[dbo].[Product] AS p, [aroniumdb].[dbo].[Users] AS u WHERE DocumentId =(SELECT TOP 1[Id] FROM [aroniumdb].[dbo].[Document] WHERE Number LIKE '%19-200%' AND CashRegisterID = '" + till + "' ORDER BY Id DESC) AND d.Id = di.DocumentId AND di.ProductId = p.Id AND d.UserId = u.Id";
			        			
			        	System.out.println(SQL);
			        	//String SQL = "select * from panda.dbo.productlist where SKU = \'" +  code + "\'";
			            //txtTamount.setText(SQL);
			            ResultSet rs = stmt.executeQuery(SQL);
			            String date = "";
		            	String number  = "";
		            	String name = "";
		            	Float price = null;
		            	Float qty = null;
		            	Float total = null;
		            	Float DocumentTotal = null;
		            	String Username = "";
			            while(rs.next()){
			            	date = rs.getString("Date");
			            	number  = rs.getString("Number");
			            	//String SKU = rs.getString("ProductID");
			            	name = rs.getString("Name");
			            	price = rs.getFloat("price");
			            	qty = rs.getFloat("Quantity");
			            	total = rs.getFloat("Total");
			            	DocumentTotal = rs.getFloat("DocumentTotal");
			            	Username = rs.getString("Username");
			            	
			            	String taxID = "";
			            	/*if (rs.getString("TaxId").equals("2")){
			            		taxID = "A";
			            	}else{
			            		taxID = "B";
			            	}*/
			            						       
			            	strSize = name.length();
						    
							if (strSize>25) {
						        name = name.substring(0, 25);
						    }
						    
						    //DBsubTotal = price * qty;
						    //total = total + DBsubTotal;
						    /*if (tax.equals(taxCode2)){
						       ItemTax = ItemTax + (DBsubTotal * (16.5/100));
						       taxableSales = taxableSales + DBsubTotal;
						    }else{*/
						    	ItemTax = ItemTax + 0.00;
						       	nontaxableSales = nontaxableSales + DBsubTotal;
						    //}
						       	printText +=  name + "   " + price + " X " + qty + "  "+ total +"\n";
			            }
			            
					    printerServices.printString("POS-Printer", "Date: " + date + "\n\n");
					    printerServices.printString("POS-Printer", "\n------------------------------------------\n");
					    printerServices.printString("POS-Printer", "Transaction No.: " + number + "\n");
						printerServices.printString("POS-Printer", printText);
						printerServices.printString("POS-Printer", "------------------------------------------\n");
						
						System.out.println("Date:" + date);
						System.out.println("Transaction:" + number);
						System.out.println(printText);
												
						String salesDetails = "";
						//salesDetails += "Sales Taxable A    : " + sbTaxable +"\n" + "Tax Total          : " + sbItemTax +"\n" + "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
						salesDetails += "Sales NonTaxable B : " + DocumentTotal +"\n" + "Total              : " + DocumentTotal +"\n"  + "------------------------------------------\n";
						printerServices.printString("POS-Printer", salesDetails);
						System.out.println(salesDetails);
						System.out.println("Operator :" + Username);
						printerServices.printString("POS-Printer", "------------------------------------------\n");
						printerServices.printString("POS-Printer", "Operator: " + lblUsernameTxt.getText() + "\n\n");
						printerServices.printString("POS-Printer", "------------------------------------------\n");
						printerServices.printString("POS-Printer", "Get Well Soon!!!!!\n\n\n\n\n");
						printerServices.printBytes("POS-Printer", cutP);
						model.setRowCount(0);
						txtTotal.setText("");
						txtChange.setText("");
						txtTamount.setText("");
						txtCustomerName.setText("");
						txtBcode.grabFocus();
						btnPrintRcpt.setEnabled(false);
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
			
			if (e.getSource()==btnPrintRcpt){
				try {
					File file = new File("C:\\libraries\\config.txt");
					File posErrorLog = new File("C:\\libraries\\logs\\errorLog");
					@SuppressWarnings("resource")
					BufferedWriter LogWriter = new BufferedWriter(new FileWriter(posErrorLog));
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					String till = reader.readLine();
					String countOrders = "SELECT COUNT(dbo.Document.DocumentTypeId) as \"rows\" from Document where DocumentTypeId = 2";
					LogWriter.append(countOrders);
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
			        	System.out.println(lblUsernameTxt.getText());
			        	String userName = lblUsernameTxt.getText();
			        	
			        	String input = "";
			        	input = txtCustomerName.getText();
			        	// Split the string by " - "
			            String[] parts = input.split(" - ", 2);
			            // Extract values
			            String customerId = parts[0];  // "Id"
			            String customerName = parts[1];    // "Customer Name"
			            // Print results
			            System.out.println("Number: " + customerId);
			            System.out.println("Text: " + customerName);
			            
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
			            String transactSQL = "insert into [aroniumdb].[dbo].[document] (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule, SessionId) values ('19-200-"+paddedOrders +"',(SELECT Id FROM Users WHERE Username = '" + userName + "'), '" + customerId + "', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0," + sessionId3 + ")";
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
					           if (tax.equals("B")){
					        	   salesTax = priceAfterTax * 0.165;
					        	   priceBeforeTax = priceAfterTax - salesTax;
					        	   //itemTotalAfterTax = (itemTotalAfterTax - (salesTax * QtySold));
					           }
					           else{
					        	   priceBeforeTax = priceAfterTax;
					        	   itemTotalAfterTax = itemTotalAfterTax;
					           }
					           String upDateDocItem = "insert into [aroniumdb].[dbo].[DocumentItem](DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
					           		" ((select id from aroniumdb.dbo.Document where OrderNumber =" + numOrders + "), " +  id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0)";
					           System.out.println(upDateDocItem);
					           stmtSt.executeUpdate(upDateDocItem);
			        	}	            
			        }
			        // Handle any errors that may have occurred.
			        catch (SQLException e1) {
			            e1.printStackTrace();
			        }
					for (int x = 0; x == 1; x++){
						for (int count = 0; count < model.getRowCount(); count++){
				           String id = (model.getValueAt(count, 0).toString());
				           double sale = Double.parseDouble(model.getValueAt(count, 3).toString());
				           String SQL = "SELECT * FROM [aroniumdb].[dbo].[stock] where productId = " +  id;
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
								        	String updateSQL = "update [aroniumdb].[dbo].[stock] set Quantity = " + balance + "where ProductId = " +  id;
								        	System.out.println(updateSQL);
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
				    }
					//Connect(comPort);
					String Tandered = txtTamount.getText();
					String Pmode = "P";
					PrinterService printerServices = new PrinterService();
					byte[] cutP = new byte[] { 0x1d, 'V', 1 };
					Double DBsubTotal = 0.0;
					Double Total = 0.0;
					Double ItemTax = 0.0;
					char taxCode = 'A';
			        String taxCode2 = String.valueOf(taxCode);
			        Double taxTotal = 0.0;
			        Double taxableSales = 0.0;
			        Double nontaxableSales = 0.0;
			        String printText = "";
			        /*BufferedImage logoImage = ImageIO.read(new File("C:\\Libraries\\company_logo.png")); // Replace with the correct path to your logo
			        ByteArrayOutputStream baos = new ByteArrayOutputStream();
			        ImageIO.write(logoImage, "png", baos);
			        byte[] logoBytes = baos.toByteArray();
			        printerServices.printBytes("POS-Printer", logoBytes);*/
			     // Load and print the logo
			        String logoPath = "C:\\Libraries\\company_logo.png"; // Your logo path
			        byte[] logoBytes = ImagePrinter.convertImageToESC_POS(logoPath);
			        printerServices.printBytes("POS-Printer", logoBytes); // Print the logo
			        printerServices.printString("POS-Printer", Bold_on + " GIFTED HANDS PVT CLINIC" + Bold_off + "\n\n");
		            printerServices.printString("POS-Printer", "Lilongwe \n");
		            printerServices.printString("POS-Printer", "Our Confidence Is Our Capabality \n");
		            printerServices.printString("POS-Printer", "Cell: +265 886 498 222  \n      +265 995 767 137 \n");
		            printerServices.printString("POS-Printer", "Date: " + mydate + "\n\n");
					printerServices.printString("POS-Printer", "Transaction No.: 19-200-"+paddedOrders);
					int strSize = 0;
					printerServices.printString("POS-Printer", "\n------------------------------------------\n");
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
				           total = total + DBsubTotal;
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
				           printText +=  desc + "   " + price + " X " + q + "  " + DBsubTotal +"  "+ tax +"\n";
					}
					printerServices.printString("POS-Printer", printText);
					printerServices.printString("POS-Printer", "------------------------------------------\n");
					BigDecimal sbCash = new BigDecimal(txtTamount.getText());
					sbCash = sbCash.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbTaxable = new BigDecimal(taxableSales);
					sbTaxable = sbTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbNonTaxable = new BigDecimal(nontaxableSales);
					sbNonTaxable = sbNonTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbItemTax = new BigDecimal(ItemTax);
					sbItemTax = sbItemTax.setScale(2, RoundingMode.HALF_UP);
					String salesDetails = "";
					//salesDetails += "Sales Taxable A    : " + sbTaxable +"\n" + "Tax Total          : " + sbItemTax +"\n" + "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
					salesDetails += "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
					printerServices.printString("POS-Printer", salesDetails);
					printerServices.printString("POS-Printer", "------------------------------------------\n");
					printerServices.printString("POS-Printer", "Operator: " + lblUsernameTxt.getText() + "\n\n");
					printerServices.printString("POS-Printer", "------------------------------------------\n");
					printerServices.printString("POS-Printer", "Get Well Soon!!!!!\n\n\n\n\n");
					printerServices.printBytes("POS-Printer", cutP);
					model.setRowCount(0);
					txtTotal.setText("");
					txtChange.setText("");
					txtTamount.setText("");
					txtCustomerName.setText("");
					txtBcode.grabFocus();
					btnPrintRcpt.setEnabled(false);								
				} catch (IllegalArgumentException | IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				txtBcode.grabFocus();				
			}
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
					String code = txtBcode.getText();
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
		@Override
		public void keyReleased(java.awt.event.KeyEvent arg0) {
			// TODO Auto-generated method stub
	
		}
		@Override
		public void keyTyped(java.awt.event.KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
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


