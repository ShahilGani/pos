import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;

import org.mindrot.jbcrypt.BCrypt;

public class main extends JFrame {
    private JButton btnSubmit, btnCancel;
    private JTextField txtUserName;
    private JPasswordField txtPasswd;
    private JLabel lblUserName, lblPasswd;
    private static main loginForm;

    public static void main(String[] args) {
        loginForm = new main();
        loginForm.showLoginForm();
    }

    public void showLoginForm() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(200, 100, 600, 400);
        this.setLayout(null);
        this.setTitle("Login");
        this.getContentPane().setBackground(new Color(56, 62, 74));

        lblUserName = new JLabel("User Name:");
        lblUserName.setFont(new Font("Century Gothic", Font.PLAIN, 18));
        lblUserName.setForeground(Color.WHITE);
        lblUserName.setBounds(50, 50, 100, 30);
        this.add(lblUserName);

        txtUserName = new JTextField();
        txtUserName.setFont(new Font("Century Gothic", Font.PLAIN, 18));
        txtUserName.setBounds(155, 50, 300, 30);
        this.add(txtUserName);

        lblPasswd = new JLabel("Password:");
        lblPasswd.setFont(new Font("Century Gothic", Font.PLAIN, 18));
        lblPasswd.setForeground(Color.WHITE);
        lblPasswd.setBounds(50, 150, 100, 30);
        this.add(lblPasswd);

        txtPasswd = new JPasswordField();
        txtPasswd.setBounds(155, 150, 300, 30);
        this.add(txtPasswd);

        btnSubmit = new JButton("Login");
        btnSubmit.setFont(new Font("Century Gothic", Font.BOLD, 18));
        btnSubmit.setBackground(new Color(51, 245, 12));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBounds(240, 190, 105, 35);
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        this.add(btnSubmit);

        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Century Gothic", Font.BOLD, 18));
        btnCancel.setBackground(new Color(227, 20, 20));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBounds(355, 190, 100, 35);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        this.add(btnCancel);

        this.setVisible(true);
    }

    private void performLogin() {
        String uname = txtUserName.getText();
        String passwd = String.valueOf(txtPasswd.getPassword());

        if (uname.isEmpty() || passwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username or password cannot be empty.");
            return;
        }

        // Load configuration from file
        Properties config = loadDatabaseConfig("C:/Libraries/config-mysql.txt");
        if (config == null) {
            JOptionPane.showMessageDialog(this, "Failed to load configuration.");
            return;
        }

        // Extract database parameters
        String ip = config.getProperty("ip");
        String dbUser = config.getProperty("dbUser");
        String dbPass = config.getProperty("dbPass");
        String dbName = config.getProperty("dbName");
        String port = config.getProperty("port");

        String connectionUrl = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";

        try (Connection con = DriverManager.getConnection(connectionUrl, dbUser, dbPass);
             PreparedStatement pstmt = con.prepareStatement("SELECT Password FROM users WHERE UserName = ?")) {

            pstmt.setString(1, uname);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("Password");
                    System.out.println(storedHash);
                    String compatibleHash = storedHash.replace("$2y$", "$2a$");
                    if (checkPassword(passwd, compatibleHash)) {
                        JOptionPane.showMessageDialog(this, "Login Successful");
                        new UI();
                        loginForm.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect password");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "User not found");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }


 // Helper method to load database credentials
    private Properties loadDatabaseConfig(String filePath) {
        Properties props = new Properties();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String port = br.readLine();        // Line 1: COM3 (ignored for DB)
            String ip = br.readLine();          // Line 2: IP address
            String dbUser = br.readLine();      // Line 3: Database user
            String dbPass = br.readLine();      // Line 4: Database password
            String dbName = br.readLine();      // Line 5: Database name
            String reserved = br.readLine();    // Line 6: 1 (ignored for DB)

            // Populate properties
            props.setProperty("ip", ip);
            props.setProperty("dbUser", dbUser);
            props.setProperty("dbPass", dbPass);
            props.setProperty("dbName", dbName);
            props.setProperty("port", "3306"); // Default MySQL port

            return props;

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading configuration: " + e.getMessage());
            return null;
        }
    }


    // Helper method to check password hash
    private boolean checkPassword(String plainPassword, String storedHash) {
        return BCrypt.checkpw(plainPassword, storedHash);
    }
    
}
