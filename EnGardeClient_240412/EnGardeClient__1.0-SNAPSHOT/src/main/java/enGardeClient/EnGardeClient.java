/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package enGardeClient;

import engarde.gui.Constants;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author ktajima
 */
public class EnGardeClient {

    public static void main(String[] args) {
        //Nomal Mode
        Constants.setFiles();
        //
        //Debug for ktajima
//        File dataDir = new File("C:\\Users\\ktajima\\OneDrive - 独立行政法人 国立高等専門学校機構\\NetBeansProjects\\novaluna\\img");
//        Constants.setFiles(dataDir);
        //
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EnGardeClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(EnGardeClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(EnGardeClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(EnGardeClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        mainFrame frame = new mainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        

    }
}
