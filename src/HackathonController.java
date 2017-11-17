

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.swing.JTextField;

import org.apache.spark.graphx.Edge;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import scala.Tuple2;

/**
 *
 * @author payen8u
 */
public class HackathonController implements Initializable{
    
    private String nomFichier="data-train.tab";
    private String cheminHTML="file:///C:/Users/Tagre/git/ScalaHackathon/Vizu.html";
    @FXML WebView webView;
    @FXML private WebEngine webEngine;
    @FXML public ListView<String> listeColonnes;
    @FXML public TextField champLabel;
    @FXML public TextField champProtein;
    @FXML public TextField champNbrNoeuds;
    @FXML public TextField  champFichier;
    @FXML public Label champDetails;
    private String details="";
    @FXML public ObservableList<Integer> selectedIndices;
    @FXML public Button printGraphButton;
    public LectureData lect=new LectureData(nomFichier);
    Tuple2<Tuple2<Object, Tuple2<String, String>>[], Edge<Object>[]> nodesEdges;
    private String label="";
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    	champNbrNoeuds.setText("100");
    	champFichier.setText(nomFichier);
        webEngine = webView.getEngine();
        //webEngine.load("file:///C:/Users/Tagre/git/ScalaHackathon/Vizu.html");
        
        listeColonnes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        refreshListAttribut();
		
		//Remplissage de la colonne d'attributs
		
		
		champDetails.setText("Détails :\nFichier utilisé : "+nomFichier+"\nLabel du graph : "+label);
    }
    
    protected void refreshListAttribut() {
    	BufferedReader flotFiltre;
		String filtre;
    	try {
			flotFiltre = new BufferedReader(new FileReader(nomFichier));
			filtre=flotFiltre.readLine();
			String[] elements=filtre.split("\t");
			label=elements[2];
			champLabel.setText(label);
			ObservableList<String> items =FXCollections.observableArrayList ();
	        for (int i=2;i<elements.length;i++){
	            items.add(elements[i]);
	        }
	        listeColonnes.setItems(items);
	        flotFiltre.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    @FXML
    protected void handleGraphButtonAction(ActionEvent event) {
    	ObservableList<String> listeSelected = listeColonnes.getSelectionModel().getSelectedItems();
    	System.out.println(listeSelected.get(0));
    	
    	ArrayList<String> liste=new ArrayList<String>();
    	liste.addAll(listeSelected);
    	System.out.println(liste.size());
    			
    	String[] simpleArray = new String[ liste.size() ];
    	liste.toArray( simpleArray );
    	
    	System.out.println("y "+liste.toString());
    	System.out.println(lect.selectRow().size());
    	lect.RowSelection(simpleArray);
    	System.out.println(lect.selectRow().size());
    	
    	String[][] data = lect.Parsing(Integer.parseInt(champNbrNoeuds.getText())+1);
    	nodesEdges = lect.RunGraph(data, lect.correspondance());
    	
    	details="Détails :\nFichier utilisé : "+nomFichier+"\nLabel du graph : "+label+"\nNombre de noeuds : "+nodesEdges._1.length+
    			"\nNombre de liaisons : "+ nodesEdges._2.length;
    	
    	printGraphButton.setTextFill(Color.RED);
    }
    
    @FXML
    protected void handleLabelButtonAction(ActionEvent event) {
    	label=champLabel.getText();
        lect.ChangeLabel(champLabel.getText());
        handleGraphButtonAction(event);
        
    }
    @FXML
    protected void handlePrintGraphButton(ActionEvent event) {
    	champDetails.setText(details);
    	printGraphButton.setTextFill(Color.BLACK);
    	webEngine.load(cheminHTML);
    }
    
    
    @FXML
    protected void handleSearchProteinButtonAction(ActionEvent event) {
        lect.searchProteine(champProtein.getText(), nodesEdges._1, nodesEdges._2);
    }
    
    @FXML
    protected void closeWindowsAction(ActionEvent event) {
    	Stage stage = (Stage) champDetails.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    protected void printAboutAction(ActionEvent event) {
    	Stage stage = (Stage) champDetails.getScene().getWindow();
    	final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("About");
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Application réalisée dans le cadre du Hackathon 2017\n\n"
        		+ "Crédits :\n"
        		+ "CLOUET Maël\n"
        		+ "HUSSON Aurore\n"
        		+ "MARTINS Melvin\n"
        		+ "PAYEN Typhaine"));
        Scene dialogScene = new Scene(dialogVbox, 300, 130);
        dialog.setScene(dialogScene);
        dialog.show();
    }
    
    @FXML
    protected void printHelpAction(ActionEvent action) {
    	Stage stage = (Stage) champDetails.getScene().getWindow();
    	final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("Voici l'aide"));
        Scene dialogScene = new Scene(dialogVbox, 300, 150);
        dialog.setScene(dialogScene);
        dialog.show();
    }
    
    @FXML
    protected void handleMajParameterButton(ActionEvent action) {
    	label=champLabel.getText();
        lect.ChangeLabel(champLabel.getText());
        nomFichier=champFichier.getText();
        refreshListAttribut();
        
        
        
        handleGraphButtonAction(action);
        
    }
    
    
}
