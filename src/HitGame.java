import javax.swing.JFrame;

public class HitGame {
   
   public static void main(String[] args) {
      JFrame window = new JFrame("Hit Game (Beta)");
      GPanel content = new GPanel();
      window.setContentPane(content);
      window.pack();
      window.setLocation(100,100);
      window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
      window.setResizable(false); 
      window.setVisible(true);
   }
   
}