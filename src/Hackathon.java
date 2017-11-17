

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



/**
 *
 * @author payen8u
 */
public class Hackathon extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("hackathon2.fxml"));
        
        Scene scene = new Scene(root, 1200, 700);
        
        stage.setTitle("Big Graph Data");
        stage.setScene(scene);
        //stage.setFullScreen(true);
        
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
