package firstGUI;
import javax.swing.*;

public class gui extends JFrame{
	public static void main(String [] arg){
		new gui();
	}
	
	private gui(){
		this.setSize(400, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
}
