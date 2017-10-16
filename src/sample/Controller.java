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
    private TreeMap<String,String> opcode = new TreeMap<>(String.  CASE_INSENSITIVE_ORDER);
    private Hashtable<String,Integer> errorFlag = new Hashtable<>();//to catching Errors and there lines
    private Map<Integer,String> intermediate = new HashMap<>();
    private int proramLength =0;
    private String programName;


    @FXML
    TextArea textArea  ;

    @FXML
    Button ok = new Button();

    private ObservableList<CharSequence> code;

    public Controller(){
        opcode.put("start","00");
        opcode.put("lda" ,"00000000");
        opcode.put("ldx" ,"00000100");
        opcode.put("sta" ,"00001100");
        opcode.put("stx" ,"00010000");

        opcode.put("add" ,"00011000");
        opcode.put("sub" ,"00011100");
        opcode.put("mul" ,"00100000");
        opcode.put("div" ,"00100100");

        opcode.put("comp","00101000");

        opcode.put("jle" ,"00111000");//is wrong 'jle' -> jlt
        opcode.put("jeq" ,"00110000");
        opcode.put("jgt" ,"00110100");

        opcode.put("jsub","01001000");
        opcode.put("rsub","01001100");



    }

    public void okOnAction() throws IOException {
        code = textArea.getParagraphs();
        lexical(code);
    }

    private void lexical(ObservableList<CharSequence> code) throws IOException {

        //pass 1
        Iterator<CharSequence> itPass1 =code.iterator();
        int[] operIndex= new int[code.size()+1];

        for (int x : operIndex)x=1;//make it all one by default unless someone but a label before instruction

        while (itPass1.hasNext()){ //iterate throw each line (each iteration for one line)
            String statement = itPass1.next().toString();

            lineCounter++;//to detecting errors
            LinkedList<String> words = splitIgnoreSpaces(statement);//spite line statement into separated words and ','
            
                        if(lineCounter == 1){//to decide start address -> if not found key word 'start' starting address is = 0 by default (initialization value)
                            int index=0;
                            boolean status = false;
                                for(Iterator<String> i =words.iterator();i.hasNext();){
                                if(i.next().equalsIgnoreCase("start")){status = true; break;}
                                index++;
                                }
                                      if(status){// if start operation found in the first line of program
                                        startAdress = PC = Integer.valueOf(words.get(++index));
                                        System.out.println(PC);}
                        }//end if it is start address يجب أن يغير هذا الكود يوجد به خطأ

                        String instruction ="";
                        if (words.get(0).charAt(0)!='/'){// to check if it is comment line then skip it
                            int index=0;
                            for (Iterator<String> i =words.iterator();i.hasNext();){// iterate throw each word in the line
                                String word =i.next();
                                if (index > 1)break; /*because in pass one we just check the labels addresses(or values) and the operations
                                                    , in this case labels cannot be out of first word in the line and operation cannot be out of 1st or 2nd word in the line*/

                                if(index == 0 && !opcode.containsKey(word))
                                    if(isLabel(word))operIndex[lineCounter]= 2;

                                else if(opcode.containsKey(word)) {
                                        instruction = word;
                                    if (word.equalsIgnoreCase("resw")){PC+=3*Integer.valueOf(i.next()); index++;}
                                    else if (word.equalsIgnoreCase("resb")){PC+=Integer.valueOf(i.next());  index++;}
                                    else if (word.equalsIgnoreCase("byte")){/*calculate how many bytes*/ index++;}
                                    else PC+=3;

                                }//end of if it is operation
                                 //else error
                                index++;
                            }//end of for iterate throw each word in the line
            }//end if not comment

            intermediate.put(lineCounter,instruction);

        }// end of while loop -> iterate throw each line


        proramLength = PC - startAdress;
        // end of pass one


        //printing intermediate file -> just for testing
        /* for (Map.Entry<Integer, String> entry : intermediate.entrySet())
        {
            System.out.println("line: "+entry.getKey() + "  ->" + entry.getValue());
        }*/

     //----------------------------------------------------------
     //pass 2
        programName = "myObjectCode";
        FileWriter  objFile = null;
        try {
              objFile = new FileWriter(new File("d:\\"+ programName +".obj"),true);
        } catch (IOException e) {
            e.printStackTrace();
            errorFlag.put("object File couldn't be created",-1);
        }

        lineCounter=0;
        String operandAddress = "";

        for(Map.Entry<Integer, String> intermid : intermediate.entrySet()){
            String instruction =intermid.getValue();
            lineCounter++;
            String statement = code.get(intermid.getKey()).toString();
            LinkedList<String> words = splitIgnoreSpaces(statement);//spite line statement into separated words and ','

            String str = words.get(operIndex[intermid.getKey()]);
            if (instruction.equalsIgnoreCase("word")||instruction.equalsIgnoreCase("byte"))//in case of instruction is ether 'word' or 'byte'
                operandAddress =convert2bin(str);// convert operand value into binary
            else if (instruction.equalsIgnoreCase("rsub"))operandAddress = "0";
            else {//other instructions
                if (Character.isDigit(str.charAt(0))) operandAddress =convert2bin(str);// if operand is not a label
                else  {// in case of operand is a label
                    if (symbolTable.containsKey(str)) operandAddress =Integer.toBinaryString(symbolTable.get(str));
                    else {operandAddress = "0"; errorFlag.put("undefined Symbol",lineCounter); } //in case of operand is a label BUT label is unknown
                }

            }

            objFile.write(opcode.get(instruction)+" "+operandAddress);
        }

    }//end of lexical function

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

    private String convert2bin(String chars){
        String binary="";
            for (char a : chars.toCharArray())
                binary += Integer.toBinaryString(a);

      return binary;
    }

    private boolean isNumeric(String str){

        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

}
