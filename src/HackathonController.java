package hackathon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 *
 * @author payen8u
 */
public class HackathonController implements Initializable{
    
    @FXML
    WebView webView;
    private WebEngine webEngine;
    private TextField textField;
    public ListView listeColonnes;
    public Text champLabel;
    public Text champProtein;
    public ObservableList<Integer> selectedIndices;
    //public LectureData lect=new LectureData("data-train.tab");
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        webEngine = webView.getEngine();
        webEngine.load("file:///home/etudiants/payen8u/NetBeansProjects/Hackathon/src/hackathon/Vizu.html");
        
        listeColonnes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        BufferedReader flotFiltre;
		String filtre;
		Scanner scannerl;
		
		try {
			flotFiltre = new BufferedReader(new FileReader("data-train.tab"));
			filtre=flotFiltre.readLine();
			String[] elements=filtre.split("\t");
			ObservableList<String> items =FXCollections.observableArrayList ();
	        for (String element:elements){
	            items.add(element);
	        }
	        listeColonnes.setItems(items);
	        flotFiltre.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
    }
    
    @FXML
    protected void handleLabelButtonAction(ActionEvent event) {
        champLabel.setText(textField.getText());
    }
    
    @FXML
    protected void handleProteinButtonAction(ActionEvent event) {
        champProtein.setText(textField.getText());
    }
    
    
    @FXML
    protected void handleValidateButtonAction(ActionEvent event) {
        selectedIndices = listeColonnes.getSelectionModel().getSelectedIndices();
    }
}
