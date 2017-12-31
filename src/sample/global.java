package sample;

import java.util.ArrayList;
import java.util.HashMap;

class global {
    final int ID = 0;

    final int FORMAT3 = 3;
    //final int FORMAT4 = 4;
    final int START = 5;
    final int NUM = 6;
    final int REGISTER = 7;
    final int WORD = 8;
    final int BYTE = 9;
    final int RESW = 10;
    final int RESB = 11;
    final int PLUS = 12;
    final int AAT = 13;
    final int HASH = 14;
    final int COMMA = 15;
    final int STRING = 16;
    final int HEX = 17;
    final int END = 18;
    final int QUOTE = 19;
    final int BYTEVLA = 20;
    final int BASE = 21;
    final int ORG = 22;
    final int EQU = 23;
    final int EQUAL = 24;
    final int STAR = 25;
    final int CONST = 26;
    final int LTORG = 27;



    //----------------errors numbers
    final int numFollByChar = 1111;
    final int unExpectedToken = 1112;


    HashMap<Integer,String>  tokensWithStrings = new HashMap<>();
    static ArrayList<entry> opTable = new ArrayList<>();




    global(){
        opTable.add(new entry("rsub",FORMAT3,"01001100"));

        opTable.add(new entry("lda",FORMAT3,"00000000"));
        opTable.add(new entry("ldx",FORMAT3,"00000100"));
        opTable.add(new entry("sta",FORMAT3,"00001100"));
        opTable.add(new entry("stx",FORMAT3,"00010000"));

        opTable.add(new entry("add",FORMAT3,"00011000"));
        opTable.add(new entry("sub",FORMAT3,"00011100"));
        opTable.add(new entry("mul",FORMAT3,"00100000"));
        opTable.add(new entry("div",FORMAT3,"00100100"));

        opTable.add(new entry("comp",FORMAT3,"00101000"));
        opTable.add(new entry("jlt" ,FORMAT3,"00111000"));
        opTable.add(new entry("jeq" ,FORMAT3,"00110000"));
        opTable.add(new entry("jgt" ,FORMAT3,"00110100"));
        opTable.add(new entry("jsub",FORMAT3,"01001000"));
        //------------------
        opTable.add(new entry("stch",FORMAT3, "01001000"));
        opTable.add(new entry("LDCH",FORMAT3, "01010000"));
        opTable.add(new entry("ADDF",FORMAT3, "01001000"));
        opTable.add(new entry("COMPF",FORMAT3,"01001000"));
        opTable.add(new entry("DIVF",FORMAT3, "01001000"));
        opTable.add(new entry("TIX",FORMAT3,  "00101100"));
        opTable.add(new entry("STCH",FORMAT3, "01010100"));
        opTable.add(new entry("LDT",FORMAT3,  "01001000"));
        opTable.add(new entry("LDS",FORMAT3,  "01001000"));
        opTable.add(new entry("STL",FORMAT3,  "01001000"));
        opTable.add(new entry("LDL",FORMAT3,  "01001000"));

        //--------------------/






        tokensWithStrings.put(ID,"ID");
        tokensWithStrings.put(FORMAT3,"FORMAT3");
        tokensWithStrings.put(START,"START");
        tokensWithStrings.put(END,"END");
        tokensWithStrings.put(NUM,"NUM");
        tokensWithStrings.put(WORD,"WORD");
        tokensWithStrings.put(BYTE,"BYTE");
        tokensWithStrings.put(RESW,"RESW");
        tokensWithStrings.put(PLUS,"PLUS '+' ");
        tokensWithStrings.put(AAT,"AAt '@' ");
        tokensWithStrings.put(COMMA,"COMMA ',' ");
        tokensWithStrings.put(HASH,"HASH '#' ");
        tokensWithStrings.put(QUOTE,"QUOTE '\'' ");
        tokensWithStrings.put(BASE,"BASE directive");
        tokensWithStrings.put(ORG ,"ORG directive");
        tokensWithStrings.put(EQU ,"EQU directive");
        tokensWithStrings.put(EQUAL,"EQUAL '=' ");
        tokensWithStrings.put(STAR ,"STAR '*'");
        tokensWithStrings.put(CONST ,"CONST variable");
        tokensWithStrings.put(numFollByChar,"number followed by character without space in between");

    }


}
