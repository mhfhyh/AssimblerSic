package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


public class parser extends semantic{
    @FXML
    TextArea inputScreen;

    @FXML
    protected TextArea errorScreen;



    @FXML TableView<entry> SymbolTableView;
    @FXML TableColumn<entry,String> LabelColumn;
    @FXML TableColumn<entry,Integer> TypeColumn ;
    @FXML TableColumn<entry,Integer> AddressColumn ;
    @FXML TextArea machineCodeScreen ;
    @FXML Button toHexButton;
    @FXML TableView<machineCode> intermediateTable;
    @FXML TableColumn<machineCode,Integer> lineCol;
    @FXML TableColumn<machineCode,Integer> pcCol;
    @FXML TableColumn<machineCode,Integer> baseCol;
    @FXML TableColumn<machineCode,Integer> formatCol;
    @FXML TableColumn<machineCode,String> insCol;
    @FXML TableColumn<machineCode,String> addressCol;
    @FXML TableColumn<machineCode,String> restCol;


    private int lookahead = -1;
    private int numOfWord;
    private ArrayList<machineCode> intermediate = new ArrayList<>();
    private int format = -1 ;
    private static String addressLabel = null;
    private String insCode = null;
    private String codeRest = null;
    private String constLabel = null;
    private int Base = 0;
    private int lineBase = 0;
    private int linePc = 0;
    private int progEndAddress;
    private int ExecuteLabel=-1;
    private static HashMap<Integer,LiteralTable> LiteralTable = new HashMap<>();
    private boolean OrgFlag =false;
    private int OrgPC = -1;
    private String modification= null;



    private ObservableList<CharSequence> code ;

    private void initialize(){
        lookBack = -1;
        symbolFound = null;
        Base =0;
        lineBase =0;
        linePc =0;
        tokenVal = -1;
        PC = 0;
        label = null;
        errorNum = -1;
        lineCounter = 0;
        errorScreen.setText("");
        SymbolTable = new ArrayList<>();
        intermediate = new ArrayList<>();
        LiteralTable = new HashMap<>();
        progEndAddress = -1;
        words = new LinkedList<>();
        currWordIndex = 0;
        tokenVal = -1;
        PC = 0;
        label = null;
        errorNum = -1;
        byteFlag = false;
        SymbolTable = new ArrayList<>();
        constLabel = null;
        OrgFlag =false;
        OrgPC = -1;
        ExecuteLabel= -1;
        modification= null;

    }
    public void okOnAction(){
        errorMsg.bindBidirectional(errorScreen.textProperty());
        //binding the textProperty of 'errorScreen TextArea' with 'errorMsg StringProperty' the result is each time we made a change on 'errorMsg' it do the same change on 'textProperty of errorScreen'

        initialize();
        //initialize the variables with zero values to began new assembling . At each time user press ok program began from scratch

        code = inputScreen.getParagraphs();//get the code from inputScreen text area
        nextSentence();// fetch the first instruction
            sic();

        //for symbol screen table
        ObservableList<entry> list=FXCollections.observableArrayList(SymbolTable);
        LabelColumn.setCellValueFactory(new PropertyValueFactory<>("mnemonic_labelName"));
        TypeColumn.setCellValueFactory(new PropertyValueFactory<>("token"));
        AddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        SymbolTableView.setItems(list);

        //--- for intermediate table in GUI
        ObservableList<machineCode> list1 = FXCollections.observableArrayList(intermediate);
        lineCol.setCellValueFactory(new PropertyValueFactory<>("line"));
        pcCol.setCellValueFactory(new PropertyValueFactory<>("pc"));
        baseCol.setCellValueFactory(new PropertyValueFactory<>("base"));
        formatCol.setCellValueFactory(new PropertyValueFactory<>("format"));
        insCol.setCellValueFactory(new PropertyValueFactory<>("InsCode"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("addressLabel"));
        restCol.setCellValueFactory(new PropertyValueFactory<>("codeRest"));

        intermediateTable.setItems(list1);


        writeToObjectFile();//write to object file
    }


//starting of the grammars
    private void sic(){

        header();
        body();
        tail();

        pass2();



    }



    private void header(){
        String currLabel = label;// getting the 'ID' string value from the lexical analyzer
        match(ID);
        match(START); // match the word start
        PC = tokenVal;// getting the starting address integer value from the lexical analyzer
        match(NUM);   // match the starting address
        SymbolTable.add(new entry(currLabel,ID,PC));//adding the starting program label with its address to symbol table

    }
    private void tail(){
        match(END);
        match(ID);

        progEndAddress = PC;

        if (tokenVal == -3)
            ExecuteLabel = symbolFound.getAddress();

        else error(lineCounter+" Undefined Label after END "+label);

    }
    private void body(){

        if (lookahead == ID){
            String currLabel = label;// getting the 'ID' string value from the lexical analyzer
            constLabel = label;
            match(ID);
                SymbolTable.add(new entry(currLabel,ID,PC));
            rest();
            body();
        }
        else if(lookahead == FORMAT3) {
            stmt();
            body();
        }

    }
    private void rest(){
        if(lookahead == FORMAT3)
            stmt();
        else if (lookahead == WORD || lookahead == BYTE || lookahead == RESW ||lookahead == RESB)
            data();
        else
            errorWithNext(lineCounter+" Missed token. ID should followed by (Instruction OR data directive OR constant directive .But found "+tokensWithStrings.get(lookahead)+" "+constLabel);//error
    }
    private void stmt(){
        linePc =PC;
        lineBase = Base;

           // case FORMAT3:
                            format = 3;//instruction line format
                            insCode = label;// getting the instruction binary code as string value from the lexical analyzer
                match(FORMAT3);
                PC += 3;
                z();


    }
    private void z(){//called from stmt->format 3 and format 4

        if (lookahead == ID) {
            addressLabel = label;// getting the first operand string value from the lexical analyzer
            match(ID);
            index();
        }
        else errorWithNext(lineCounter+" syntax error: un unexpected token After \'"+tokensWithStrings.get(lookBack)+"\' . Expected: ID found: "+tokensWithStrings.get(lookahead));
    }
    private void index(){
        if (lookahead == COMMA){
            match(COMMA);
                        format++;
            match(REGISTER);
        }


    }
    private void data(){
        linePc =PC;
        lineBase = Base;

        switch (lookahead){
            case WORD:
                format = 0;
                match(WORD);
                            addressLabel = fill(Integer.toBinaryString(tokenVal),23,false);// getting the word value and save it as hex ,
                            //insCode = "--";

                match(NUM);
                PC += 3;
                break;

            case BYTE:
                            format =0;
                 match(BYTE);
                 byteValue();
                            PC += addressLabel.length();
                            //insCode = "--";
                 match(QUOTE);
                break;

            case RESW:
                match(RESW);
                match(NUM);
                PC += 3*tokenVal;
                break;

            case RESB:
                match(RESB);
                match(NUM);
                PC += tokenVal;
                break;
           /* default: error("un unexpected token. found: "+tokensWithStrings.get(lookahead));//error*/
        }
    }
    private void byteValue(){
        /*boolean literalFlag = false;
        if (lookBack == EQUAL) literalFlag =true;*/
        if (lookahead == STRING){
            match(STRING);
            match(QUOTE);
            addressLabel = textToAsciiBin(label);// getting the char byte value
            match(BYTEVLA);

        }

        else if (lookahead == HEX) {
            match(HEX);
            match(QUOTE);

            if (label.length() == 0)
            error(lineCounter+" empty byte Value");
            else  addressLabel = new BigInteger(label,16).toString(2);// getting the hex byte value then convert it to Binary
            match(BYTEVLA);

        }
        //else  error("un unexpected token. found: "+tokensWithStrings.get(lookahead)); //error

    }
//-------------------end of grammars-------------------

    private void match(int tok){

        //if (lookahead != tok) mark it as error and look for the next
        if (lookahead != tok){ error(lineCounter +" :syntax error: un unexpected token After \'"+tokensWithStrings.get(lookBack)+"\' . Expected: "+tokensWithStrings.get(tok)+" found: "+tokensWithStrings.get(lookahead));}

        if(currWordIndex == numOfWord)
            nextSentence();
        else{
             lookBack = lookahead;
             lookahead = lexical();
        }
    }

    private void pass2() {

        String output = "";

        for (machineCode line : intermediate) {
            // System.out.println("Line: "+line.getLine()+"Format: "+line.getFormat()+"InsCode: "+line.getInsCode()+"AddressLabel: "+line.getAddressLabel()+"CodeRest: "+line.getCodeRest());
            switch (line.getFormat()) {

                case  0:
                    output += line.getAddressLabel() + "\n";
                    break;
                case 3:
                    output += line.getInsCode()+"0"+ optimizeAddress(line.getLine(),line.getPc(),line.getAddressLabel()) + "\n";
                    break;//3 -> format 3
                case 4:
                    output += line.getInsCode()+"1"+ optimizeAddress(line.getLine(),line.getPc(),line.getAddressLabel()) + "\n";
                    break;//,4 -> format 3 with indexing
                default: error(line.getLine()+" Unknown format -> " +line.getFormat());
            }

        }
        machineCodeScreen.setText(output);
        toHexButton.setDisable(false);
        toHexButton.setText("Hex");
    }
    /*this function 'optimizeAddressLabel()' has tow tasks first determine wither given label is already defined in the SymbolTable or not.
   If it is not mark it as error and return null.If it already defined return the address of that label .
   Second task is to writing modification record
   */
  private String optimizeAddress(int line, int pc, String addressLabel){
      int index = SymbolTable.indexOf(new entry(addressLabel,0,0));//at the beginning we check if the label is defined before
      if (index != -1){
          String address = Integer.toBinaryString(SymbolTable.get(index).getAddress());
          writeModificationRecord(pc,Integer.toBinaryString(address.length()));
          return fill(address,20,false);
      }

      error(line+" undefined Label "+addressLabel);
      return null;
  }


/*
   Task 2 is checking wither given address fit in 12 bits if it is yes return that address as string.
   If it is not make the address relative to PC or Base and return that address */


    private void nextSentence(){
        writeInte();// writing the intermediate list current line

        lineCounter++;//each time this method is called it is indicate ending of current line and jumping to next line

        while (lineCounter <= code.size()){

            words = splitIgnoreSpaces(code.get(lineCounter-1).toString());//code.get(lineCounter-1).toString() -> 'code.get()' is return the text line in type of 'CharSequence' that's why we use '.toString()'
            if (words != null && words.size() != 0){//in case of null (or words.size()==0)  it is mean the line ether line comment or blank line , will skip until find a statement line , or reach the end of program.

                // since this 'splitIgnoreSpaces' function return NOT null object , we will reinitialize the variables to  do parsing in the new line
                currWordIndex = 0;
                numOfWord = words.size();
                lookBack = -1;// to use it as indicator of new line -> we use it i org()
                lookahead = lexical();// we do lexical analyzing to the first word in the new line
                break;
            }

            lineCounter++;
        }
        if (lineCounter > code.size()){lineCounter = 0;}//that's mean it reach the end of the program
    }

    //Mark error with given message 'errorMessage' and find next token
    private void errorWithNext(String errorMessage){
        error(errorMessage); //syntax error
        if(currWordIndex == numOfWord)
            nextSentence();
        else{
            lookBack =lookahead;
            lookahead = lexical();
        }
    }


//----------------------------------------------
    public void toHexOnAction(){

        ObservableList<CharSequence> out = machineCodeScreen.getParagraphs();
        String mOut;

        if(toHexButton.getText().equalsIgnoreCase("hex")){
            mOut = convertToHex(out);
            toHexButton.setText("Binary");
        }
       else {
            mOut = convertToBin(out);
            toHexButton.setText("Hex");
        }

        machineCodeScreen.setText(mOut.toUpperCase());
    }

    private void writeInte(){// write into intermediate list
        if (lineCounter != 0 && (insCode != null || addressLabel != null)){ // case of insCode != null mean its an instruction line not a directive
            intermediate.add(new machineCode(lineCounter,linePc, lineBase,format,insCode,addressLabel,codeRest));
            format = -1;
            insCode = null;
            addressLabel = null;
            codeRest = null;

        }
    }

    private  void writeModificationRecord(int pc,String addressLength){
        String pcS =Integer.toBinaryString(pc+2);
        //Col.1 M
        if (modification == null)modification ="M";// remove M for first time
        else modification += "M";
        //Col.2-7 starting location of the address field to be modified
        modification += fill(pcS,6,false);
        //Col.8-9 length of the address field to be modified in bits
        modification += fill(addressLength,2,false);
        //new line
        modification += "\n";
    }



   private void writeToObjectFile(){
        String programName =SymbolTable.get(0).getMnemonic_labelName();
        int ProgramStartAddress = SymbolTable.get(0).getAddress();
        String ProgramLength = Integer.toBinaryString(progEndAddress-ProgramStartAddress);
       FileWriter writer = null;

       try {
           writer = new FileWriter(new File("D:"+programName+".sic"));
       } catch (IOException e) {
           e.printStackTrace();
           error(e.getMessage());
       }

       try {
           if (writer != null) {
               //Header----------
               //Col. 1 H
               writer.write("H");
               //Col. 2~7 Program name
               writer.write(String.format("%-6s",programName));
               //Col. 8~13 Starting address of object program (hex)
               writer.write(fill(String.valueOf(ProgramStartAddress),5,false));
               //Col. 14-19 Length of object program in bytes (hex)
               writer.write(fill(ProgramLength,5,false)+"\n\n");
               writer.flush();


               int counter = -1;
               machineCode intemLine ;
               Iterator<CharSequence> it = machineCodeScreen.getParagraphs().iterator();
               while (it.hasNext()&& ++counter < intermediate.size()){
                  CharSequence ch = it.next();
                   intemLine =intermediate.get(counter);
                  //Text record----------
                  // Col.1 T
                  writer.write("T");
                  // Col.2~7 Starting address for object code in this record (hex)
                  writer.write(Integer.toBinaryString(intemLine.getPc()));
                  //Col. 8~9 Length of object code in this record in bytes (hex)
                  writer.write(fill(Integer.toBinaryString(ch.length()),2,false));
                  //Col. 10~69 Object code, represented in hex (2 col. per byte)
                  writer.write(ch.toString()+"\n");
                  writer.flush();
                }
                //End Record
               String End= fill(Integer.toBinaryString(ExecuteLabel),6,false);
               writer.write("\nE"+End+"\n\n");

                //Modification Record
               if (modification!= null)
               writer.write(modification);

               writer.close();

           }


       } catch (IOException e) {
           e.printStackTrace();
           error(e.getMessage());
       }
   }

}

