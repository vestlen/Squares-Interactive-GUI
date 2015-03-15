import javax.swing.*;
 
/** Swing Program Template for running as Applet 
 * 
 * 	DON'T USE THIS
 * 	IT IS FOR TESTING PURPOSES ONLY
 * 
 * */
@SuppressWarnings("serial")
public class MainWindowApplet extends JApplet {
 
   /** init() to setup the UI components */
   @Override
   public void init() {
      // Run GUI codes in the Event-Dispatching thread for thread safety
      try {
         SwingUtilities.invokeAndWait(new Runnable() { // Applet uses invokeAndWait()
            @Override
            public void run() {
               // Set the content-pane of the JApplet to an instance of main JPanel
               setContentPane(new SquintMainWindow());
            }
         });
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}