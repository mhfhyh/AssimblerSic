package sample;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;




public class Controller {
    @FXML
    TextArea textArea = new TextArea();
    @FXML
    Button ok = new Button("Ok");
    public Controller(){

     ok.setOnAction(event -> {
         ObservableList<CharSequence> code = textArea.getParagraphs();
         System.out.print(code);});

    }
    /*private String[] linesSprator(String code){
        String[] lines= new String[];
        while (int i=0; code[i])
        return
    }*/
    /*public  void lexical(String code){
    if ()

    }*/

}
