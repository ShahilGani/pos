
import java.awt.Color;
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
import java.sql.*;

public class UI{
	JFrame frame = new JFrame();
	Toolkit tk = Toolkit.getDefaultToolkit();
	int xsize = ((int) tk.getScreenSize().getWidth());
	int ysize = ((int) tk.getScreenSize().getHeight());
	JButton btnSelect, btnLogoff;
	JButton btnReprint;
	JButton btnVoid, btnSaveDiscount;
	JButton btnDiscount;
	DefaultTableModel model, discountModel;
	DefaultTableModel resultModel;
	JTable tblSalesTable,tblDiscountSalesTable ;
	JTable tblSearchResult;
	JPanel pnlKeypad;
	JButton btnSearch;
	JTextField txtSearch;
	JLabel lblBcode, lblDiscount;
	JTextField txtBcode, txtDiscount;
	JLabel lblTamount;
	JTextField txtTamount, txtSalesTotal;
	JLabel lblChange, lblSalesTotal;
	JTextField txtChange;
	JTextField txtTotal;
	JLabel lblTotal;
	JScrollPane SalesScroll, DiscountSalesScroll;
	JButton btnPrintRcpt, btnLastTrans, btnManagerMenu;
	public static InputStream inputBuffer;
	public static OutputStream outputBuffer;
	static UI fp = null;
	double tandered = 0.0;
	double changing = 0.0;
	Font f = new Font("Arial",Font.BOLD,32);
	Font sf = new Font("Arial",Font.BOLD,17);
	public static final char ESC = 27;
	public static final String Bold_on = ESC + "E";
	public static final String Bold_off = ESC + "F";
	
	/*public static void main (String [] arg){
		new UI();
	}*/
	public UI(){		
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphics.getDefaultScreenDevice();
		frame.setUndecorated(true);
		device.setFullScreenWindow(frame);
		pnlKeypad = new JPanel();
		pnlKeypad.setBackground(Color.green);
		pnlKeypad.setBounds(620, 20, 390, 400);
		pnlKeypad.setLayout(null);//(new GridLayout(4,3));
		pnlKeypad.setFont(f);
		Action act = new Action();
		printReceipt prct = new printReceipt();
		change ch = new change();
		txtSearch = new JTextField();
		txtSearch.setBounds(5, 5, 230, 50);
		txtSearch.setFont(sf);
		autoSearch instantSearch = new autoSearch();
		searchActions searchAttach = new searchActions();
		txtSearch.getDocument().addDocumentListener(instantSearch);
		txtSearch.addKeyListener(searchAttach);
		pnlKeypad.add(txtSearch);
		btnSearch = new JButton("Search");
		btnSearch.setBounds(250, 5, 130, 50);
		btnSearch.setFont(sf);
		btnSearch.setBackground(Color.ORANGE);
		btnSearch.setForeground(Color.BLACK);
		btnSearch.addActionListener(act);
		pnlKeypad.add(btnSearch);
		btnSelect = new JButton("Select");
		btnSelect.setBounds(250, 340, 130, 50);
		btnSelect.setFont(sf);
		btnSelect.setBackground(new Color(83, 152, 190));
		btnSelect.setForeground(Color.BLACK);
		btnSelect.addActionListener(act);
		final int IFW = JComponent.WHEN_IN_FOCUSED_WINDOW;
		btnSelect.getInputMap().put(KeyStroke.getKeyStroke('w'), "prct");
		btnSelect.getActionMap().put("prct", prct);
		pnlKeypad.add(btnSelect);
		model = new DefaultTableModel(){
			public boolean isCellEditable(int row, int column) {                
	            if(column==3){
	            	return true;
	            }
	            else{
	            	return false;	
	            }
	         };};
		resultModel = new DefaultTableModel(){
			public boolean isCellEditable(int row, int column) {                
            return false;
         };};
        
         
		resultModel.addColumn("SKU");
		resultModel.addColumn("Description");
		resultModel.addColumn("Price");
		tblSearchResult = new JTable(resultModel);
		tblSearchResult.addKeyListener(searchAttach);
		tblSearchResult.getColumn("SKU").setPreferredWidth(20);
		tblSearchResult.getColumn("Description").setPreferredWidth(245);
		tblSearchResult.getColumn("Price").setPreferredWidth(2);
		JScrollPane searchPane = new JScrollPane(tblSearchResult);
		tblSearchResult.setBounds(5, 20, 400, 260);
		resultModel.isCellEditable(0, 0);
		searchPane.setBounds(5, 60, 380, 260);
		pnlKeypad.add(searchPane);
		btnReprint = new JButton("Reprint");
		btnReprint.setEnabled(false);
		btnReprint.addActionListener(act);
		btnReprint.setBounds(20, 636, 120, 60);
		btnReprint.setBackground(Color.YELLOW);
		btnReprint.setForeground(Color.BLACK);
		btnReprint.setFont(sf);
		btnReprint.setMnemonic(KeyEvent.VK_F5);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		//frame.add(btnReprint);
		btnDiscount = new JButton("Discount");
		btnDiscount.addActionListener(act);
		btnDiscount.setBounds(20, 636, 150, 60);
		btnDiscount.setBackground(new Color(52,235,140));
		btnDiscount.setForeground(Color.BLACK);
		btnDiscount.setFont(sf);
		frame.add(btnDiscount);
		btnVoid = new JButton("Delete");
		btnVoid.addActionListener(act);
		btnVoid.setBounds(200, 636, 150, 60);
		btnVoid.setFont(sf);
		btnVoid.setBackground(Color.orange);
		btnVoid.setForeground(Color.BLACK);
		frame.add(btnVoid);
		btnManagerMenu = new JButton("Manager Menu");
		btnManagerMenu.addActionListener(act);
		btnManagerMenu.setBounds(370, 636, 150, 60);
		btnManagerMenu.setFont(sf);
		btnManagerMenu.setBackground(Color.CYAN);
		btnManagerMenu.setForeground(Color.BLACK);
		frame.add(btnManagerMenu);
		btnLogoff = new JButton("Logoff");
		btnLogoff.addActionListener(act);
		btnLogoff.setBounds(910, 636, 100, 60);
		btnLogoff.setFont(sf);
		btnLogoff.setBackground(Color.red);
		btnLogoff.setForeground(Color.BLACK);
		frame.add(btnLogoff);
		frame.add(pnlKeypad);
		tblSalesTable = new JTable(model);
		tblSalesTable.setBounds(20, 20, 550, 400);
		tblSalesTable.setFont(sf);
		quantity addTotal = new quantity();
		tblSalesTable.addKeyListener(addTotal);
		model.addColumn("SKU");
		model.addColumn("Name");
		model.addColumn("Tax");
		model.addColumn("Qty");
		model.addColumn("Price");
		model.addColumn("Total Price");	
		SalesScroll = new JScrollPane(tblSalesTable);
		tblSalesTable.getColumn("SKU").setPreferredWidth(20);
		tblSalesTable.getColumn("Name").setPreferredWidth(245);
		tblSalesTable.getColumn("Tax").setPreferredWidth(2);
		tblSalesTable.getColumn("Qty").setPreferredWidth(4);
		tblSalesTable.getColumn("Price").setPreferredWidth(80);
		tblSalesTable.getColumn("Total Price").setPreferredWidth(80);
		SalesScroll.setBounds(20, 20, 550, 400);
		frame.add(SalesScroll);
		lblBcode = new JLabel("Barcode or Product Code");
		lblBcode.setBounds(20, 256, 750, 400);
		lblBcode.setFont(sf);
		lblBcode.setForeground(Color.WHITE);
		frame.add(lblBcode);
		txtBcode  = new JTextField();
		txtBcode.setBounds(20, 466, 550, 60);
		txtBcode.setFont(f);
		frame.add(txtBcode);
		lblTamount = new JLabel("Tandered Amount");
		lblTamount.setBounds(20, 346, 550, 400);
		lblTamount.setFont(sf);
		lblTamount.setForeground(Color.WHITE);
		frame.add(lblTamount);
		txtTamount  = new JTextField();
		txtTamount.setBounds(20, 556, 550, 60);
		txtTamount.setFont(f);
		frame.add(txtTamount);
		lblTotal = new JLabel("Total");
		lblTotal.setBounds(620, 256, 550, 400);
		lblTotal.setFont(sf);
		lblTotal.setForeground(Color.WHITE);
		frame.add(lblTotal);
		txtTotal  = new JTextField();
		txtTotal.setFont(f);
		txtTotal.setBounds(620, 466, 300, 60);
		frame.add(txtTotal);
		lblChange= new JLabel("Change");
		lblChange.setBounds(620, 346, 400, 400);
		lblChange.setFont(sf);
		lblChange.setForeground(Color.WHITE);
		frame.add(lblChange);
		txtChange  = new JTextField();
		txtChange.setBounds(620, 556, 300, 60);
		txtChange.setFont(f);
		frame.add(txtChange);
		btnPrintRcpt = new JButton("Print Receipt");
		btnPrintRcpt.addActionListener(act);
		receiptPrinter print = new receiptPrinter();
		btnPrintRcpt.addKeyListener(print);
		btnPrintRcpt.setBounds(620, 636, 270, 60);
		txtBcode.addKeyListener(act);
		txtBcode.addActionListener(act);
		txtTamount.addKeyListener(ch);
		btnPrintRcpt.setFont(f);
		frame.add(btnPrintRcpt);		
		frame.setVisible(true);
		btnPrintRcpt.setEnabled(false);
		winFocusListener winFocus = new winFocusListener();
		frame.addWindowListener(winFocus);
		/*frame.addKeyListener(new KeyAdapter(){
			public void KeyTyped(KeyEvent e){
				if (e.getKeyCode()==KeyEvent.VK_F2){
					txtTamount.grabFocus();				
				}
				if (e.getKeyCode()==KeyEvent.VK_F3){
					txtSearch.grabFocus();				
				}
			}
		});*/
	}
	
	public UI(String string) {
		// TODO Auto-generated constructor stub
		
	}
	private class managerMenu extends JFrame{
		//Toolkit tk2 = Toolkit.getDefaultToolkit();
		//int xsize = ((int) tk2.getScreenSize().getWidth());
		//int ysize = ((int) tk2.getScreenSize().getHeight());
		
		public managerMenu(){
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
			GraphicsEnvironment graphics2 = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice device2 = graphics2.getDefaultScreenDevice();
			//this.setUndecorated(true);
			//device2.setFullScreenWindow(this);
			this.setVisible(true);
			this.setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
		}
	}
	private class discountWindow extends JDialog implements ActionListener{
		Toolkit tk2 = Toolkit.getDefaultToolkit();
		int xsize = ((int) tk2.getScreenSize().getWidth());
		int ysize = ((int) tk2.getScreenSize().getHeight());
		private class discountKeys implements KeyListener{

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				if(arg0.getKeyCode()==KeyEvent.VK_ENTER){
					BigDecimal totalsales = new BigDecimal(txtSalesTotal.getText());
					totalsales = totalsales.setScale(2, RoundingMode.HALF_UP);
					float fTotalSales = 0;
					fTotalSales = Float.parseFloat((totalsales).toString());
					BigDecimal discount = new BigDecimal(txtDiscount.getText());
					discount = discount.setScale(2, RoundingMode.HALF_UP);
					Double discountedTotal = 0.0;
					System.out.println(fTotalSales);
					discountedTotal = (double) (fTotalSales - (fTotalSales * (Float.parseFloat((discount).toString())/100)));
					txtSalesTotal.setText(discountedTotal.toString());
					System.out.println(fTotalSales);
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
		public discountWindow(){
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			//this.setExtendedState(JFrame.MAXIMIZED_BOTH);
			GraphicsEnvironment graphics2 = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice device2 = graphics2.getDefaultScreenDevice();
			this.setUndecorated(true);
			this.setLayout(null);
			device2.setFullScreenWindow(this);
			this.getContentPane().setBackground(Color.DARK_GRAY);
			btnSaveDiscount = new JButton("Save");
			btnSaveDiscount.setFont(sf);
			this.add(btnSaveDiscount);
			btnSaveDiscount.setBounds(20, 636, 150, 60);
			btnSaveDiscount.setBackground(new Color(52,235,140));
			btnSaveDiscount.setForeground(Color.BLACK);
			btnSaveDiscount.addActionListener(this);
			discountModel = new DefaultTableModel();
			tblDiscountSalesTable = new JTable(discountModel);
			tblDiscountSalesTable.setBounds(20, 20, 1000, 400);
			tblDiscountSalesTable.setFont(sf);
			quantity addTotal = new quantity();
			tblDiscountSalesTable.addKeyListener(addTotal);
			discountModel.addColumn("SKU");
			discountModel.addColumn("Name");
			discountModel.addColumn("Tax");
			discountModel.addColumn("Qty");
			discountModel.addColumn("Price");
			discountModel.addColumn("Total Price");	
			DiscountSalesScroll = new JScrollPane(tblDiscountSalesTable);
			tblDiscountSalesTable.getColumn("SKU").setPreferredWidth(20);
			tblDiscountSalesTable.getColumn("Name").setPreferredWidth(245);
			tblDiscountSalesTable.getColumn("Tax").setPreferredWidth(2);
			tblDiscountSalesTable.getColumn("Qty").setPreferredWidth(4);
			tblDiscountSalesTable.getColumn("Price").setPreferredWidth(80);
			tblDiscountSalesTable.getColumn("Total Price").setPreferredWidth(80);
			DiscountSalesScroll.setBounds(20, 20, 1000, 400);
			this.add(DiscountSalesScroll);
			lblDiscount = new JLabel("Discount %");
			lblDiscount.setBounds(20, 256, 750, 400);
			lblDiscount.setFont(sf);
			lblDiscount.setForeground(Color.WHITE);
			this.add(lblDiscount);
			txtDiscount  = new JTextField();
			txtDiscount.setBounds(20, 466, 550, 60);
			txtDiscount.setFont(f);
			discountKeys discKeys = new discountKeys();
			txtDiscount.addKeyListener(discKeys);
			lblSalesTotal = new JLabel("Sales Total");
			lblSalesTotal.setBounds(20, 346, 550, 400);
			lblSalesTotal.setFont(sf);
			lblSalesTotal.setForeground(Color.WHITE);
			this.add(lblSalesTotal);
			txtSalesTotal  = new JTextField();
			txtSalesTotal.setBounds(20, 556, 550, 60);
			txtSalesTotal.setFont(f);
			this.add(txtSalesTotal);
			//txtTotal.setText(txtTotal.getText());
			double SalesTotal = 0.0;
			for(int row =0; row < model.getRowCount(); row++){
				Object[] rowData = new Object[model.getColumnCount()];
				for (int col = 0; col < model.getColumnCount(); col++){
					rowData[col] = model.getValueAt(row, col);
				}
				discountModel.addRow(rowData);
				double Discountselling = Double.parseDouble(model.getValueAt(row, 3).toString());
		        double dq = Double.parseDouble(model.getValueAt(row, 4).toString());
		        SalesTotal = SalesTotal + (Discountselling * dq);
		        System.out.println(SalesTotal);
		        BigDecimal stbdPrice = new BigDecimal(SalesTotal);
		        txtSalesTotal.setText((stbdPrice).toString());
			}
			this.add(txtDiscount);
			/*lblTamount = new JLabel("Tandered Amount");
			lblTamount.setBounds(20, 346, 550, 400);
			lblTamount.setFont(sf);
			lblTamount.setForeground(Color.WHITE);
			frame.add(lblTamount);*/
			this.setVisible(true);
			
			//this.setDefaultCloseOperation(this.HIDE_ON_CLOSE);
			
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if (arg0.getSource() == btnSaveDiscount) {
			    try {
			        // File to read MySQL configuration
			        File file = new File("C:\\libraries\\config-mysql.txt");
			        File posErrorLog = new File("C:\\libraries\\logs\\errorLog");
			        BufferedWriter LogWriter = new BufferedWriter(new FileWriter(posErrorLog));
			        BufferedReader reader = new BufferedReader(new FileReader(file));
			        
			        String comPort = reader.readLine(); // Printer COM port
			        String ip = reader.readLine(); // MySQL Server IP
			        String user = reader.readLine(); // MySQL User
			        String pass = reader.readLine(); // MySQL Password
			        String DBName = reader.readLine(); // MySQL Database Name
			        String till = reader.readLine(); // Till info

			        String countOrders = "SELECT COUNT(documenttypeid) as 'rows' FROM Document WHERE documenttypeid = 2";
			        LogWriter.append(countOrders);
			        
			        int numOrders = 0;
			        String total = txtTotal.getText();
			        String discountTotal = txtSalesTotal.getText();
			        String paddedOrders = "";
			        LocalDate mydate = LocalDate.now();
			        LocalDateTime dateTime = LocalDateTime.now();
			        DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			        String formatedDate = dateTime.format(ObjdateFormated);
			        
			        // MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
			        
			        try (Connection con2 = DriverManager.getConnection(connectionUrl); Statement stmtSt = con2.createStatement();) {
			            ResultSet rsSt = stmtSt.executeQuery(countOrders);
			            while (rsSt.next()) {
			                numOrders = rsSt.getInt("rows");
			                numOrders = numOrders + 1;
			                paddedOrders = String.format("%06d", numOrders);
			            }

			            String transactSQL = "INSERT INTO document (Number, UserId, CustomerId, CashRegisterId, OrderNumber, Date, StockDate, Total, IsClockedOut, DocumentTypeId, WarehouseId, ReferenceDocumentNumber, InternalNote, Note, DueDate, Discount, DiscountType, PaidStatus, DateCreated, DateUpdated, DiscountApplyRule) "
			                    + "VALUES ('19-200-" + paddedOrders + "', '1', '1', '" + till + "', '" + numOrders + "', '" + mydate + "', '" + formatedDate + "', '" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate + "', '" + formatedDate + "', 0)";
			            stmtSt.executeUpdate(transactSQL);

			            for (int count = 0; count < discountModel.getRowCount(); count++) {
			                String id = discountModel.getValueAt(count, 0).toString();
			                double QtySold = Double.parseDouble(discountModel.getValueAt(count, 3).toString());
			                double priceAfterTax = Double.parseDouble(discountModel.getValueAt(count, 4).toString());
			                double itemTotalAfterTax = Double.parseDouble(discountModel.getValueAt(count, 5).toString());
			                double priceBeforeTax = priceAfterTax; // Assume no tax if not otherwise specified

			                String upDateDocItem = "INSERT INTO DocumentItem (DocumentId, ProductId, Quantity, ExpectedQuantity, PriceBeforeTax, Discount, DiscountType, Price, ProductCost, PriceAfterDiscount, Total, PriceBeforeTaxAfterDiscount, TotalAfterDocumentDiscount, DiscountApplyRule) "
			                        + "VALUES ((SELECT id FROM document WHERE OrderNumber = " + numOrders + "), " + id + ", " + QtySold + ", 0, " + priceBeforeTax + ", 0, 0, " + priceAfterTax + ", " + id + ", " + itemTotalAfterTax + ", " + itemTotalAfterTax + ", " + priceBeforeTax + ", " + itemTotalAfterTax + ", 0)";
			                stmtSt.executeUpdate(upDateDocItem);
			            }
			        } catch (SQLException e1) {
			            e1.printStackTrace();
			        }

			        // Handle stock update logic (using MySQL)
			        for (int count = 0; count < model.getRowCount(); count++) {
			            String id = model.getValueAt(count, 0).toString();
			            double sale = Double.parseDouble(model.getValueAt(count, 3).toString());
			            String SQL = "SELECT * FROM stock WHERE productId = " + id;
			            try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement()) {
			                ResultSet rs = stmt.executeQuery(SQL);
			                if (!rs.next()) {
			                    // If stock doesn't exist, insert new record
			                    float stock = -1;
			                    double balance = stock; // Default balance
			                    String updateSQL = "INSERT INTO stock (productID, warehouseId, Quantity) VALUES (" + id + ", 1, " + balance + ")";
			                    stmt.executeUpdate(updateSQL);
			                } else {
			                    // If stock exists, update quantity
			                    float stock = rs.getFloat("Quantity");
			                    double balance = stock - sale;
			                    String updateSQL = "UPDATE stock SET Quantity = " + balance + " WHERE ProductId = " + id;
			                    stmt.executeUpdate(updateSQL);
			                }
			            } catch (SQLException e1) {
			                e1.printStackTrace();
			            }
			        }

			        // Print receipt logic (no changes needed for MySQL connection)
			        PrinterService printerServices = new PrinterService();
			        byte[] cutP = new byte[] { 0x1d, 'V', 1 };
			        Double DBsubTotal = 0.0;
			        Double ItemTax = 0.0;
			        String taxCode2 = "A"; // Example tax code
			        String printText = "";

			        printerServices.printString("POS-Printer2", "POS-Printer2\nShop\nDate: " + mydate + "\n");
			        printerServices.printString("POS-Printer2", "Transaction No.: 19-200-" + paddedOrders);
			        printerServices.printString("POS-Printer2", "\n------------------------------------------\n");

			        // Handle item printing
			        for (int count = 0; count < discountModel.getRowCount(); count++) {
			            String desc = discountModel.getValueAt(count, 1).toString();
			            String tax = discountModel.getValueAt(count, 2).toString();
			            String price = discountModel.getValueAt(count, 4).toString();
			            String q = discountModel.getValueAt(count, 3).toString();
			            DBsubTotal = Double.parseDouble(price) * Double.parseDouble(q);
			            total += DBsubTotal;

			            if (tax.equals(taxCode2)) {
			                ItemTax += (DBsubTotal * (16.5 / 100)); // Example tax rate
			            }

			            printText += desc + "   " + price + " X " + q + "  " + DBsubTotal + "  " + tax + "\n";
			        }

			        printerServices.printString("POS-Printer2", printText);
			        printerServices.printString("POS-Printer2", "------------------------------------------\n");
			        printerServices.printString("POS-Printer2", "Sales Taxable A: " + ItemTax + "\n");
			        printerServices.printString("POS-Printer2", "Thank you for shopping with us!!!\n\n\n\n");

			        printerServices.printBytes("POS-Printer2", cutP);
			        discountModel.setRowCount(0);
			        txtDiscount.setText("");
			        txtSalesTotal.setText("");
			        btnPrintRcpt.setEnabled(false);
			        
			    } catch (Exception e1) {
			        e1.printStackTrace();
			    }
			}

		}
	}
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
				File file = new File("C:\\libraries\\config-mysql.txt");
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String comPort = reader.readLine();
				String ip = reader.readLine();
				String user = reader.readLine();
				String pass = reader.readLine();
				String DBName = reader.readLine();
				reader.close();
								
				String code = txtSearch.getText();

				// MySQL Connection URL
		        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
				//JOptionPane.showMessageDialog(null, connectionUrl);
			        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT * FROM product where product.name like \'%" +  code + "%\'";
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
	private class printReceipt extends AbstractAction{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			System.out.print("working");
		}
		
	}
	private class quantity implements KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
				// TODO Auto-generated method stub
				int row = tblSalesTable.getSelectedRow();
				int column = tblSalesTable.getSelectedColumn();
				String  quantity =   (model.getValueAt(row, 3).toString());
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
					File file = new File("C:\\libraries\\config-mysql.txt");
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
					String countOrders = "SELECT COUNT(documenttypeid) as \"rows\" from Document where documenttypeid = 2";
					LogWriter.append(countOrders);
					System.out.println(countOrders);
					int numOrders = 0;
					String total = txtTotal.getText();
					String paddedOrders = "";
					LocalDateTime dateTime = LocalDateTime.now();
					DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String formatedDate = dateTime.format(ObjdateFormated);
					//SimpleDateFormat simpleDate = new SimpleDateFormat(mydate.toString());
					// MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
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
			        	String transactSQL = "insert into document (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule) values ('19-200-"+paddedOrders +"','1', '1', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0)";
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
					           String upDateDocItem = "insert into DocumentItem(DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
					           		" ((select id FROM Document where ordernumber =" + numOrders + "), " +  id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0)";
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
				           String SQL = "SELECT * FROM stock where productId = " +  id;
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
								        	String updateSQL = "update stock set Quantity = " + balance + "where ProductId = " +  id;
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
					printerServices.printString("POS-Printer2", Bold_on + " Shop" + Bold_off + "\n");
		            printerServices.printString("POS-Printer2", "Shop \n");
		            printerServices.printString("POS-Printer2", "Cell:  \n");
		            printerServices.printString("POS-Printer2", "Date: " + mydate + "\n\n");
					printerServices.printString("POS-Printer2", "Time: " + now.format(formatter) + "\n\n");
					printerServices.printString("POS-Printer2", "Transaction No.: 19-200-"+paddedOrders);
					int strSize = 0;
					printerServices.printString("POS-Printer2", "\n------------------------------------------\n");
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
					printerServices.printString("POS-Printer2", printText);
					printerServices.printString("POS-Printer2", "------------------------------------------\n");
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
					printerServices.printString("POS-Printer2", salesDetails);
					//calcSumTotalPaidAmnt(Pmode, Tandered);
					//totalCash();
					printerServices.printString("POS-Printer2", "Thank for shopping with us!!!!!\n\n\n\n\n");
					printerServices.printBytes("POS-Printer2", cutP);
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
		        			JOptionPane.showMessageDialog(null, "Wonetseni kuti ndalama zomwe kasitoma wapeleka ndizokwanila");
		        			txtTamount.grabFocus();
		        			//btnPrintRcpt.setEnabled(false);
		        		}
				}
				else{
					JOptionPane.showMessageDialog(null, "Chonde lowetsani ndalama zomwe customer wapaleka");
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
	private class searchActions implements ActionListener, KeyListener{

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.getKeyCode()==KeyEvent.VK_ENTER){
				try {
					File file = new File("C:\\libraries\\config-mysql.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					reader.close();
					int row = tblSearchResult.getSelectedRow();
					String code = tblSearchResult.getValueAt(row, 0).toString();
					// MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
								        
					try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
						String SQL = "SELECT * FROM product, ProductTax where product.Id = \'" +  code + "\' and product.Id = ProductTax.ProductId";
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
			
			/*if(arg0.getKeyCode()==KeyEvent.VK_F4){
				tblSearchResult.transferFocus();
			}*/
			
			if(arg0.getKeyCode()==KeyEvent.VK_SHIFT){
				tblSearchResult.requestFocus();
				tblSearchResult.changeSelection(0, 0, false, false);
				System.out.println("test");
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

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}

	private class Action implements ActionListener, KeyListener
	{
		protected Action()
		{
			
		}
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==btnManagerMenu){
				managerMenu newMenu = new managerMenu();
			}
			if (e.getSource()==btnDiscount){				
				discountWindow discountForm = new discountWindow();
			}
			
			if (e.getSource()==btnLogoff){
				frame.dispose();
			
				try {
					resultModel.setRowCount(0);
					File file = new File("C:\\libraries\\config-mysql.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					reader.close();
									
					Double VATSales = 0.0;
					Double NVATSales = 0.0;
					LocalDate mydate = LocalDate.now();
					PrinterService printerServices = new PrinterService();
					byte[] cutP = new byte[] { 0x1d, 'V', 1 };

					String connectionUrl = "jdbc:sqlserver://" + ip + ":1433;databaseName=" + DBName + ";user=" + user + ";password=" + pass ;
					//JOptionPane.showMessageDialog(null, connectionUrl);
			        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT DocumentItem.Price, DocumentItem.Total, Document.Number FROM DocumentItem, Document, ProductTax Where DocumentItem.DocumentId = Document.Id AND DocumentItem.ProductId = ProductTax.ProductId and ProductTax.TaxId = '1' and Number LIKE '19-200%' AND Document.Date = '" + mydate + "'";
			        	//String SQL = "select * from panda.dbo.productlist where name like \'%" +  code + "%\'";
			        	System.out.println(SQL);
			            ResultSet rs = stmt.executeQuery(SQL);
			            while(rs.next()){
			            	String vprice = rs.getString("Total");
			            	System.out.println(NVATSales);
			            	VATSales = VATSales + (Double.parseDouble(vprice));
			            }
			            System.out.println(VATSales);
			            BigDecimal bdVATSales = new BigDecimal(VATSales);
			            bdVATSales = bdVATSales.setScale(2, RoundingMode.HALF_UP);
			            rs.close();
			            String SQL2 = "SELECT DocumentItem.Price, DocumentItem.Total, Document.Number FROM DocumentItem, Document, ProductTax Where DocumentItem.DocumentId = Document.Id AND DocumentItem.ProductId = ProductTax.ProductId and ProductTax.TaxId = '2' and Number LIKE '19-200%' AND Document.Date = '" + mydate + "'";
			            System.out.println(SQL2);
			            rs = stmt.executeQuery(SQL2);
			            while(rs.next()){
			            	String price = rs.getString("Total");
			            	System.out.println(price);
			            	NVATSales = NVATSales + (Double.parseDouble(price));
			            }
			            System.out.println(NVATSales);
			            BigDecimal bdNVATSales = new BigDecimal(NVATSales);
			            bdNVATSales = bdNVATSales.setScale(2, RoundingMode.HALF_UP);
			            System.out.println(NVATSales);
			            Double VAT = VATSales * (16.5/100);
			            BigDecimal bdVAT = new BigDecimal(VAT);
			            bdVAT = bdVAT.setScale(2, RoundingMode.HALF_UP);
			            System.out.println(bdVAT);
			            Double SalesTotal = 0.0;
			            SalesTotal = NVATSales + VATSales;
			            BigDecimal bdSalesTotal = new BigDecimal(SalesTotal);
			            bdSalesTotal = bdSalesTotal.setScale(2, RoundingMode.HALF_UP);
			            System.out.println(bdSalesTotal);
			            printerServices.printString("POS-Printer2", Bold_on + " Shop" + Bold_off + "\n");
			            printerServices.printString("POS-Printer2", " Shop \n");
			            printerServices.printString("POS-Printer2", "Cell:  \n");
			            printerServices.printString("POS-Printer2", "Date: " + mydate + "\n\n");
						printerServices.printString("POS-Printer2", "End of Day Sales Receipt");
						//int strSize = 0;
						printerServices.printString("POS-Printer2", "\n------------------------------------------\n");
						printerServices.printString("POS-Printer2", "Sales Taxable: " + bdVATSales + "\n");
						printerServices.printString("POS-Printer2", "VAT: " + bdVAT + "\n");
						printerServices.printString("POS-Printer2", "Sales Not Taxable: " + bdNVATSales + "\n");
						printerServices.printString("POS-Printer2", "Sales Total: " + bdSalesTotal + "\n\n\n\n\n\n");
						printerServices.printBytes("POS-Printer2", cutP);
			            
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
			if (e.getSource()==btnSearch){
				try {
					resultModel.setRowCount(0);
					File file = new File("C:\\libraries\\config-mysql.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					reader.close();
									
					String code = txtSearch.getText();

					// MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
					//JOptionPane.showMessageDialog(null, connectionUrl);
			        try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT * FROM product where product.name like \'%" +  code + "%\'";
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
				tblSearchResult.requestFocus();
			}						
			
			if (e.getSource()==btnSelect){
				try {
					File file = new File("C:\\libraries\\config-mysql.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					reader.close();
					int row = tblSearchResult.getSelectedRow();
					String code = tblSearchResult.getValueAt(row, 0).toString();
					// MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
								        
					try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
						String SQL = "SELECT * FROM product, ProductTax where product.Id = \'" +  code + "\' and product.Id = ProductTax.ProductId";
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
			if(e.getSource()==btnReprint){
				
				try {
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
			        printerServices.printString("POS-Printer2", Bold_on + " Shop" + Bold_off + "\n");
		            printerServices.printString("POS-Printer2", "Shop \n");
		            printerServices.printString("POS-Printer2", "Cell: \n");
					
					//printerServices.printString("POS-Printer2", mydate + "\n\n");
					//printerServices.printString("POS-Printer2", "Transaction No.: 19-200-"+paddedOrders);
					int strSize = 0;
					printerServices.printString("POS-Printer2", "\n------------------------------------------\n");
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
				           printText +=  desc + "  " + price + " X " + q + "  " + DBsubTotal +"  "+ tax +"\n";
				           //printerServices.printString("EPSON TM-T20II Receipt", desc + "  " + price + " X " + q + "  " + DBsubTotal +"  "+ tax +"\n");
				           //sellItem(desc, tax, price, q);
				           System.out.println(printText);
					}
					printerServices.printString("POS-Printer2", printText);
					printerServices.printString("POS-Printer2", "------------------------------------------\n");
					BigDecimal sbCash = new BigDecimal(txtTotal.getText());
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
					printerServices.printString("POS-Printer2", salesDetails);
					//calcSumTotalPaidAmnt(Pmode, Tandered);
					//totalCash();
					printerServices.printString("POS-Printer2", "Thank for shopping with us!!!!!\n\n\n\n\n");
					printerServices.printBytes("POS-Printer2", cutP);
					//closingFiscalReceipt();
					//disconnect();
					model.setRowCount(0);
					txtTotal.setText("");
					txtChange.setText("");
					txtTamount.setText("");
					txtBcode.grabFocus();
					btnPrintRcpt.setEnabled(false);
					//model.addRow(new Object[] {"Description", "TAX ID", "Quantity", "Price"});
					
					btnReprint.setEnabled(false);
				} catch (IllegalArgumentException e2) {
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
					// MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
					model.setRowCount(0);
					btnReprint.setEnabled(true);
					try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	//single till requires no till number
						//String SQL = "SELECT * FROM [aroniumdb].[dbo].[DocumentItem], [aroniumdb].[dbo].[Product], [aroniumdb].[dbo].[ProductTax] WHERE DocumentId =(SELECT TOP 1[Id] FROM [aroniumdb].[dbo].[Document] WHERE Number LIKE '%19-200%' AND CashRegisterID = '" + till + "' ORDER BY Id DESC) AND [aroniumdb].[dbo].[DocumentItem].ProductId = [aroniumdb].[dbo].[Product].Id AND [aroniumdb].[dbo].[Product].Id = [aroniumdb].[dbo].[ProductTax].ProductId";
			        	String SQL = "SELECT * FROM DocumentItem, Product, ProductTax WHERE DocumentId =(SELECT TOP 1[Id] FROM Document WHERE Number LIKE '%19-200%' AND CashRegisterID = '" + till + "' ORDER BY Id DESC) AND DocumentItem.ProductId = Product.Id AND Product.Id = ProductTax.ProductId";
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
			            	if (rs.getString("TaxId").equals("1")){
			            		taxID = "B";
			            	}else{
			            		taxID = "A";
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
					File file = new File("C:\\libraries\\config-mysql.txt");
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
					String countOrders = "SELECT COUNT(Document.DocumentTypeId) as \"rows\" from Document where DocumentTypeId = 2";
					LogWriter.append(countOrders);
					int numOrders = 0;
					String total = txtTotal.getText();
					String paddedOrders = "";
					LocalDate mydate = LocalDate.now();
					LocalDateTime dateTime = LocalDateTime.now();
					DateTimeFormatter ObjdateFormated = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					String formatedDate = dateTime.format(ObjdateFormated);
					//SimpleDateFormat simpleDate = new SimpleDateFormat(mydate.toString());
					// MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
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
			        	String transactSQL = "insert into document (Number,UserId,CustomerId,CashRegisterId,OrderNumber,Date,StockDate,Total,IsClockedOut,DocumentTypeId,WarehouseId,ReferenceDocumentNumber,InternalNote,Note,DueDate,Discount,DiscountType,PaidStatus,DateCreated,DateUpdated,DiscountApplyRule) values ('19-200-"+paddedOrders +"','1', '1', '" + till + "', '"+ numOrders +"','" + mydate + "','" + formatedDate + "','" + total + "', 0, 2, 1, '', '', '', '" + mydate + "', 0, 0, 2, '" + formatedDate +"','" + formatedDate + "', 0)";
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
					           String upDateDocItem = "insert into DocumentItem(DocumentId,ProductId,Quantity,ExpectedQuantity,PriceBeforeTax,Discount,DiscountType,Price,ProductCost,PriceAfterDiscount,Total,PriceBeforeTaxAfterDiscount,TotalAfterDocumentDiscount,DiscountApplyRule) values " +
					           		" ((select id from Document where OrderNumber =" + numOrders + "), " +  id + "," + QtySold + "," + 0 + "," + priceBeforeTax + ", 0, 0," + priceAfterTax + "," +  id + ","  + itemTotalAfterTax + "," + itemTotalAfterTax + "," + priceBeforeTax + "," + itemTotalAfterTax + ", 0)";
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
				           String SQL = "SELECT * FROM stock where productId = " +  id;
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
								        	String updateSQL = "update stock set Quantity = " + balance + "where ProductId = " +  id;
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
			        printerServices.printString("POS-Printer2", Bold_on + " Shop" + Bold_off + "\n");
		            printerServices.printString("POS-Printer2", "shop \n");
		            printerServices.printString("POS-Printer2", "Cell:  \n");
		            printerServices.printString("POS-Printer2", "Date: " + mydate + "\n\n");
					printerServices.printString("POS-Printer2", "Transaction No.: 19-200-"+paddedOrders);
					int strSize = 0;
					printerServices.printString("POS-Printer2", "\n------------------------------------------\n");
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
					printerServices.printString("POS-Printer2", printText);
					printerServices.printString("POS-Printer2", "------------------------------------------\n");
					BigDecimal sbCash = new BigDecimal(txtTamount.getText());
					sbCash = sbCash.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbTaxable = new BigDecimal(taxableSales);
					sbTaxable = sbTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbNonTaxable = new BigDecimal(nontaxableSales);
					sbNonTaxable = sbNonTaxable.setScale(2, RoundingMode.HALF_UP);
					BigDecimal sbItemTax = new BigDecimal(ItemTax);
					sbItemTax = sbItemTax.setScale(2, RoundingMode.HALF_UP);
					String salesDetails = "";
					salesDetails += "Sales Taxable A    : " + sbTaxable +"\n" + "Tax Total          : " + sbItemTax +"\n" + "Sales NonTaxable B : " + sbNonTaxable +"\n" + "Total              : " + txtTotal.getText() +"\n" + "Cash               : " + sbCash +"\n" + "Change             : " + txtChange.getText() +"\n" + "------------------------------------------\n";
					printerServices.printString("POS-Printer2", salesDetails);
					printerServices.printString("POS-Printer2", "Thank for shopping with us!!!!!\n\n\n\n\n");
					printerServices.printBytes("POS-Printer2", cutP);
					model.setRowCount(0);
					txtTotal.setText("");
					txtChange.setText("");
					txtTamount.setText("");
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
			if (e.getSource()==btnVoid){
				int row = tblSalesTable.getSelectedRow();
				model.removeRow(row);
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
			}
			/*if (e.getSource()==btnNum1){
				JOptionPane.showMessageDialog(null, "test");				
			}*/
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
			if (arg0.getKeyCode()==KeyEvent.VK_DOWN){
				tblSearchResult.requestFocus();
				tblSearchResult.changeSelection(0, 0, false, false);
			}
			
			if(arg0.getKeyCode()==KeyEvent.VK_F6){
				tblSalesTable.requestFocus();
				tblSalesTable.changeSelection(0, 3, false, false);
	
			}
			
			if (arg0.getKeyCode()==KeyEvent.VK_F4){
				/*int rowNum = tblSearchResult.getSelectedRow();
				String selectedPLU = resultModel.getValueAt(0, rowNum).toString();
				JOptionPane.showMessageDialog(null, selectedPLU);*/
				btnReprint.grabFocus();
			}
			if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
				try {
					//JOptionPane.showMessageDialog(null, "It works");
					File file = new File("C:\\libraries\\config-mysql.txt");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String comPort = reader.readLine();
					String ip = reader.readLine();
					String user = reader.readLine();
					String pass = reader.readLine();
					String DBName = reader.readLine();
					reader.close();				
					String code = txtBcode.getText();
					// MySQL Connection URL
			        String connectionUrl = "jdbc:mysql://" + ip + ":3306/" + DBName + "?user=" + user + "&password=" + pass;
			        String PIDt ="";
			        String quantity = "1";
			        PIDt = code.substring(0,2);
			        double subtotal = 0.0;			        
			        if (PIDt.equals("20")){
			        	quantity = code.substring(7,9) + "." + code.substring(9,12);
			        	code = code.substring(2,6);
			        	//JOptionPane.showMessageDialog(null, code);
			        }
			        //JOptionPane.showMessageDialog(null, PIDt);
					try (Connection con = DriverManager.getConnection(connectionUrl); Statement stmt = con.createStatement();) {
			        	String SQL = "SELECT * FROM product, barcode, ProductTax where product.Id = barcode.ProductId and Product.Id = ProductTax.ProductId and Barcode.Value = \'" +  code + "\'";
			        	System.out.println(SQL);
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
			if(arg0.getKeyCode()==KeyEvent.VK_DOWN){
				tblSearchResult.transferFocus();
				tblSearchResult.editCellAt(0,0);
				System.out.println("test");
			}
	
		}
		@Override
		public void keyTyped(java.awt.event.KeyEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public static void disconnect() 
    {
		//sp.closePort();   
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
                    //System.out.println("Received data...");
                    //System.out.print(new String(buffer,0,len));
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
            		//System.err.print(c);
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

	 /*public static void Connect(String portName) throws Exception{

		 	System.out.println("Connect COMPort: " + portName);
		 
		 	sp = SerialPort.getCommPort(portName);
			
			sp.setComPortParameters(9600, 8, 1, 0);
			sp.setFlowControl(0);
			sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
					
			if(sp.openPort()==true)
			{
							             
	            inputBuffer = sp.getInputStream();
	            Thread.sleep(100);
	            outputBuffer = sp.getOutputStream();
	        
	            Thread.sleep(200);
	            (new Thread(new SerialWriter(outputBuffer))).start();
	            Thread.sleep(20);
	            (new Thread(new SerialReader(inputBuffer))).start();
	            
	            Thread.sleep(50);
	            System.out.println("Connected");
			}
			else
			{
				System.out.println("Failed...");
				JOptionPane.showMessageDialog(null, "Failed to connect to printer");
			}
     }*/    

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
		//mFMP.totalInCash();
	}
	
	public void sellItem (String SaleDescription, String TaxCd, String SinglePrice, String Qwantity) throws IOException, Exception, Throwable
	{
		//mFMP.sellThis(SaleDescription, TaxCd, SinglePrice, Qwantity);
	}

	/********************************************/
	public  void openNonFiscalReceipt() throws IllegalArgumentException, IOException, InterruptedException
	{
		//26h (38) Open a non-fiscal receipt.
		//mFMP.cmd38v0b0();
	}

	public  void closeNonFiscalReceipt() throws IllegalArgumentException, IOException, InterruptedException
	{
		//mFMP.cmd39v0b0();
	}
	
	public  void printNonFiscalText(String TRGT_TEXT) throws IllegalArgumentException, IOException, InterruptedException
	{
		//mFMP.cmd42v0b0(TRGT_TEXT);
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
		//mFMP.cmd48v0b0(OpCode, OpPwd, TillNmb);

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
	
		//mFMP.cmd48v0b1(OpCode, OpPwd, TillNmb, BuyerLine1, BuyerLine2, BuyerLine3, TIN, VRN);
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
		
		//mFMP.cmd49v0b1(L1, L2, TaxCd, Price, Quan, UN, Perc);
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
		//mFMP.cmd49v0b2(L1, L2, TaxCd, Price, Quan, UN, AbsSum);
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
		
		//mFMP.cmd51v0b1(ToPrint, Perc);
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
		
		//mFMP.cmd51v0b2(ToPrint, AbsSum);
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
	
		//mFMP.cmd53v0b0(PaidMode, Amount_In);
	}
	
	public  void calcSumTotalNone() throws  IllegalArgumentException, IOException
	{
		/*
	 		35H (53) CALCULATION OF A TOTAL 
		 */
	
		//mFMP.cmd53v0b1();
	}

	public  void calcSumTotalPaidMode(String PaidMode) throws  IllegalArgumentException, IOException
	{
		/*
	 		35H (53) CALCULATION OF A TOTAL 
	 		cmd53v0b0(String PaidMode, String Amount_In) 
			Parameters	
			Amount_In	The sum tendered (up to 9 meaningful symbols) 
		 */
	
		//mFMP.cmd53v0b2(PaidMode);
	}

	public  void calcSumTotalAmntIn(String Amount_In) throws  IllegalArgumentException, IOException
	{
		/*
	 		35H (53) CALCULATION OF A TOTAL 
	 		cmd53v0b0(String PaidMode, String Amount_In) 
			Parameters	
			Amount_In	The sum tendered (up to 9 meaningful symbols) 
		 */
	
		//mFMP.cmd53v0b3(Amount_In);
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
		
		//mFMP.cmd54v0b0 (Input_Text);
	}	 
	
	
	/******************************************************/
	
	//Close fiscal receipt
	public  void closingFiscalReceipt() throws  IllegalArgumentException, IOException
	{
		//mFMP.cmd56v0b0();
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
		//mFMP.cmd58v0b0(PLU, Quan, UN, Perc);
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
		//mFMP.cmd58v0b1(PLU, Quan, UN, AbsSum);
	}	
	
	/******************************************************/
	
	//Cancel Fiscal receipt
	public  void cancelFiscalReceipt() throws  IllegalArgumentException, IOException
	{
		//mFMP.cmd60v0b0();
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
		//mFMP.customCommand(84, bcData);
	}
	
	
	public  void printBarcodeTypeWithNumber(String BC_Type, String Data) throws  IllegalArgumentException, IOException
	{
		//		mFMP.cmd84v0b1(BC_Type, Data);
		String bcData = BC_Type + "," + Data;
		//System.out.println("WithNUmber" + bcData);
		//mFMP.customCommand(84, bcData);		
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

		//mFMP.customCommand(69, "2");
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

		//mFMP.customCommand(69, "0N");
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
		//mFMP.customCommand(69, "0A");
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
		
		//mFMP.cmd44v0b0 (TRGT_LINES) ;
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
		//mFMP.cmd92v0b0(LineType);
	}	
	
	/******************************************************/
	
	//Print diag information
	public  void printDiagInfo() throws  IllegalArgumentException, IOException
	{
		//mFMP.cmd71v0b0();
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
		//mFMP.customCommand(106, delayTime);
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
		//mFMP.cmd43v0b0("L", enable);
	}

	public  void autoPaperCut(String enable) throws  IllegalArgumentException, IOException
	{
		/*
		 * 	"C" Permission/rejection of the automatic cutting of paper after each receipt. After switching ON, the performance of printer 
			is defined in accordance with the setting of the switch SW1. 

			If  = 'C' - One symbol value '0' or '1', where "0" forbids and "1" permits the automatic cutting of the receipt. 

		 */
		//mFMP.cmd43v0b0("C", enable);
	}

	public  void barcodHeight(String height) throws  IllegalArgumentException, IOException
	{
		/*
		 * 	"B" Set bar code height in pixels (0.125 mm). Possible values from 24 (3 mm) to 240 (30 mm). The barcode is printed with 
				command 84 (54H). 
			If  = 'B' - A number - the height of bar code in pixels. 

		 */
				
		//mFMP.cmd43v0b0("B", height);
	}
	
}



