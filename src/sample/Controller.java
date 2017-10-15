package sample;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Controller {
    private int startAdress=0;
    private int PC=0;
    private int lineCounter=0;
    private TreeMap<String,Integer> symbolTable = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private TreeMap<String,Integer> opcode = new TreeMap<>(String.  CASE_INSENSITIVE_ORDER);
    private Hashtable<String,Integer> errorFlag = new Hashtable<>();//to catching Errors and there lines
    private Map<Integer,String> intermediate = new HashMap<>();
    private int proramLength =0;
    private String programName;


    @FXML
    TextArea textArea  ;

    @FXML
    Button ok = new Button();

    ObservableList<CharSequence> code;

    public Controller(){
        opcode.put("start",16);
        opcode.put("lda" ,0);
        opcode.put("ldx" ,1);
        opcode.put("sta" ,2);
        opcode.put("stx" ,3);

        opcode.put("add" ,4);
        opcode.put("sub" ,5);
        opcode.put("mul" ,6);
        opcode.put("div" ,7);

        opcode.put("comp" ,10);

        opcode.put("jle" ,11);
        opcode.put("jeq" ,12);
        opcode.put("jgt" ,13);

        opcode.put("jsub" ,14);
        opcode.put("rsub" ,15);



    }

    public void okOnAction(){
        code = textArea.getParagraphs();
        lexical(code);
    }

    private void lexical(ObservableList<CharSequence> code){

        //pass 1
        Iterator<CharSequence> itPass1 =code.iterator();
        while (itPass1.hasNext()){ //pass one
            String statement = itPass1.next().toString();

            lineCounter++;//to detecting errors
            LinkedList<String> words = splitIgnoreSpaces(statement);//spite line statement into separated words and ','
            
            if(lineCounter == 1){
                    int index=0;
                    boolean status=false;
                    for(Iterator<String> i =words.iterator();i.hasNext();){
                        if(i.next().equalsIgnoreCase("start")){status = true; break;}
                        index++;
                        }

                if(status){
                    startAdress = PC = Integer.valueOf(words.get(++index));
                    System.out.println(PC);}
            }
            String instruction ="";
            if (words.get(0).charAt(0)!='/'){
                int index=0;
                for (Iterator<String> i =words.iterator();i.hasNext();){
                    String word =i.next();
                    if (index > 1)break;

                    if(index == 0 && !opcode.containsKey(word)){
                        if(isLabel(word));}

                    else if(opcode.containsKey(word)) {
                            instruction =word;
                        if (word.equalsIgnoreCase("resw")){PC+=3*Integer.valueOf(i.next()); index++;}
                        else if (word.equalsIgnoreCase("resb")){PC+=Integer.valueOf(i.next());  index++;}
                        else if (word.equalsIgnoreCase("byte")){/*calculate how many bytes*/ index++;}
                        else PC+=3;

                        }
                     //else error
                    index++;
                }

            }

            intermediate.put(lineCounter,String.valueOf(opcode.get(instruction)));

    }
        proramLength = PC - startAdress;

        //printing intermediate file -> just for testing
        /* for (Map.Entry<Integer, String> entry : intermediate.entrySet())
        {
            System.out.println("line: "+entry.getKey() + "  ->" + entry.getValue());
        }*/


     //pass 2
        programName = "myObjectCode";

        try {
            FileWriter objFile = new FileWriter(new File("d:\\"+ programName +".obj"),true);
        } catch (IOException e) {
            e.printStackTrace();
            errorFlag.put("object File couldn't be created",-1);
        }

        lineCounter=0;
        /*Iterator<Integer,String> itPass2 =intermediate.;
        while (itPass2.hasNext()){
            lineCounter++;


        }*/

    }
    private LinkedList<String> splitIgnoreSpaces(String text){

        LinkedList<String> Words = new LinkedList<>();
        int index=0;
        String word="";
        for(int i=0;i<text.length();i++){

            while (i<text.length() && text.charAt(i)!= ' '){
                if(text.charAt(i)==','){
                    if (word.length()>0){
                    Words.add(word);
                    word="";}
                    Words.add(",");
                    break;
                }
                word+=text.charAt(i);
                i++;
            }

            if (word.length()>0){
                Words.add(word);
                word="";
            }

        }
        return Words;
    }

    private boolean isLabel(String label){
        if (symbolTable.containsKey(label)){errorFlag.put("duplicate label",lineCounter); return false;}
        else{ symbolTable.put(label,PC);
        return true;
        }

    }

}
