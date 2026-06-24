package main;

import com.opencsv.CSVWriter;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;


public class Utils {

    private static Utils instance;
    public static final Word<String> OMEGA_SYMBOL = Word.fromLetter("Ω");

    private static final String WORD_DELIMITER = ";";
    private static final String SYMBOL_DELIMITER = ",";

    private static String BenchmarksDir = "resources/Benchmarks/";
    public static Utils getInstance() {
        if(instance == null){
            Utils.instance = new Utils();
        }
        return instance;
    }

    public CompactDFA<String> loadDFAfromDOT(File file) throws Exception {
        Pattern kissLine = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");
        Pattern pattern = Pattern.compile("\\s*[qQsS]\\s*(\\d+)\\s*");

        BufferedReader br = new BufferedReader(new FileReader(file));

        List<String[]> trs = new ArrayList<String[]>();

        HashSet<String> abcSet = new HashSet<>();
        List<String> abc = new ArrayList<>();
        List<Integer> final_states = new ArrayList<>();

        while (br.ready()) {
            String line = br.readLine();
            Matcher m = kissLine.matcher(line);
            Matcher m2 = pattern.matcher(line);
            if (m.matches()) {
                String[] tr = new String[3];
                tr[0] = m.group(1);
                tr[1] = m.group(3);//.replace("/", "");
                if (!abcSet.contains(tr[1])) {
                    abcSet.add(tr[1]);
                    abc.add(tr[1]);
                }
                tr[2] = m.group(2);
                trs.add(tr);
            }
            else if (m2.matches()){
                String stateId = m2.group(1);
                final_states.add(Integer.parseInt(stateId));

            }
        }

        br.close();

        Collections.sort(abc);
        Alphabet<String> alphabet = Alphabets.fromCollection(abc);
        CompactDFA<String> dfa = new CompactDFA<String>(alphabet);


        Map<String,Integer> states = new HashMap<String,Integer>();
        Integer si=null,sf=null;
        Integer s0 = null;

        for (String[] tr : trs) {
            if(!states.containsKey(tr[0])) states.put(tr[0], dfa.addState());
            if(!states.containsKey(tr[2])) states.put(tr[2], dfa.addState());

            si = states.get(tr[0]);
            if(s0==null) s0 = si;
            sf = states.get(tr[2]);


            dfa.addTransition(si, tr[1], sf, null);
        }

        for (Integer st : dfa.getStates()) {
            for (String in : alphabet) {
                if(dfa.getTransition(st, in)==null){
                    dfa.addTransition(st, in, st, null);
                }
            }
        }

        for(int state_id : final_states){
            dfa.setAccepting(state_id, true);
        }

        dfa.setInitialState(s0);

        return dfa;

    }


    public CompactDFA<String> loadDFA(File f) throws Exception {

        Pattern kissLine = Pattern.compile("\\s*(\\S+)\\s+--\\s+(\\S+)\\s+->\\s+(\\S+)\\s*");
        Pattern pattern = Pattern.compile("[qQ]\\s*(\\d+)\\s*");

        BufferedReader br = new BufferedReader(new FileReader(f));

        List<String[]> trs = new ArrayList<String[]>();

        HashSet<String> abcSet = new HashSet<>();
        List<String> abc = new ArrayList<>();
        List<Integer> final_states = new ArrayList<>();

        while (br.ready()) {
            String line = br.readLine();
            Matcher m = kissLine.matcher(line);
            Matcher m2 = pattern.matcher(line);
            if (m.matches()) {
                String[] tr = new String[3];
                tr[0] = m.group(1);
                tr[1] = m.group(2);
                if (!abcSet.contains(tr[1])) {
                    abcSet.add(tr[1]);
                    abc.add(tr[1]);
                }
                tr[2] = m.group(3);
                trs.add(tr);
            }
            else if (m2.matches()){
                String stateId = m2.group(1);
                final_states.add(Integer.parseInt(stateId));

            }
        }

        br.close();

        Collections.sort(abc);
        Alphabet<String> alphabet = Alphabets.fromCollection(abc);
        CompactDFA<String> dfa = new CompactDFA<String>(alphabet);


        Map<String,Integer> states = new HashMap<String,Integer>();
        Integer si=null,sf=null;
        Integer s0 = null;

        for (String[] tr : trs) {
            if(!states.containsKey(tr[0])) states.put(tr[0], dfa.addState());
            if(!states.containsKey(tr[2])) states.put(tr[2], dfa.addState());

            si = states.get(tr[0]);
            if(s0==null) s0 = si;
            sf = states.get(tr[2]);


            dfa.addTransition(si, tr[1], sf, null);
        }

        for (Integer st : dfa.getStates()) {
            for (String in : alphabet) {
                if(dfa.getTransition(st, in)==null){
                    dfa.addTransition(st, in, st, null);
                }
            }
        }

        for(int state_id : final_states){
            dfa.setAccepting(state_id, true);
        }

        dfa.setInitialState(s0);

        return dfa;

    }

    public CompactDFA<String> loadDFA(File f, String num) throws Exception {

        Pattern kissLine = Pattern.compile("\\s*(\\S+)\\s+--\\s+(\\S+)\\s+->\\s+(\\S+)\\s*");
        Pattern pattern = Pattern.compile("[qQ]\\s*(\\d+)\\s*");

        BufferedReader br = new BufferedReader(new FileReader(f));

        List<String[]> trs = new ArrayList<String[]>();

        HashSet<String> abcSet = new HashSet<>();
        List<String> abc = new ArrayList<>();
        List<Integer> final_states = new ArrayList<>();

        while (br.ready()) {
            String line = br.readLine();
            Matcher m = kissLine.matcher(line);
            Matcher m2 = pattern.matcher(line);
            if (m.matches()) {
                String[] tr = new String[3];
                tr[0] = m.group(1);
                tr[1] = "c" + num + "_" + m.group(2);
                if (!abcSet.contains(tr[1])) {
                    abcSet.add(tr[1]);
                    abc.add(tr[1]);
                }
                tr[2] = m.group(3);
                trs.add(tr);
            }
            else if (m2.matches()){
                String stateId = m2.group(1);
                final_states.add(Integer.parseInt(stateId));

            }
        }

        br.close();

        Collections.sort(abc);
        Alphabet<String> alphabet = Alphabets.fromCollection(abc);
        CompactDFA<String> dfa = new CompactDFA<String>(alphabet);


        Map<String,Integer> states = new HashMap<String,Integer>();
        Integer si=null,sf=null;
        Integer s0 = null;

        for (String[] tr : trs) {
            if(!states.containsKey(tr[0])) states.put(tr[0], dfa.addState());
            if(!states.containsKey(tr[2])) states.put(tr[2], dfa.addState());

            si = states.get(tr[0]);
            if(s0==null) s0 = si;
            sf = states.get(tr[2]);


            dfa.addTransition(si, tr[1], sf, null);
        }

        for (Integer st : dfa.getStates()) {
            for (String in : alphabet) {
                if(dfa.getTransition(st, in)==null){
                    dfa.addTransition(st, in, st, null);
                }
            }
        }

        for(int state_id : final_states){
            dfa.setAccepting(state_id, true);
        }

        dfa.setInitialState(s0);

        return dfa;

    }

    public ProductDFA<String> loadProductDFA(File f, String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(f));
        ProductDFA<String> productDFA = null;
        int comp_num = 1;
        while (br.ready()) {
            String line = br.readLine();
            line = BenchmarksDir + dir + line;
            File file = new File(line);
            CompactDFA component;
            if (line.contains(".dot"))
                component = loadDFAfromDOT(file);
            else if (line.contains(".txt"))
                component = loadDFA(file);
            else
                throw (new IOException("File format not supported"));

            if (productDFA == null) productDFA = new ProductDFA<>(component);
            else productDFA.interleaving_parallel_composition(component);
            comp_num++;
        }
        return productDFA;
    }

    public CompactMealy<String, Word<String>> loadMealyMachineFromDot(File f) throws Exception {
        System.out.println(f.getName());

        Pattern kissLine = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");

        BufferedReader br = new BufferedReader(new FileReader(f));

        List<String[]> trs = new ArrayList<String[]>();

        HashSet<String> abcSet = new HashSet<>();

        while(br.ready()){
            String line = br.readLine();
            Matcher m = kissLine.matcher(line);
            if(m.matches()){
                String[] tr = new String[4];
                tr[0] = m.group(1);
                tr[1] = m.group(3);
                tr[3] = m.group(2);
                if(tr[1].contains("<br />")){
                    String trr[] = tr[1].split("<br />");
                    tr[1]=trr[0];
                    tr[2]=trr[1];
                    trr = tr[1].split(" \\| ");
                    for (String string : trr) {
                        String trrr[] = new String[4];
                        trrr[0]= tr[0];
                        trrr[1]= string;
                        trrr[2]= tr[2];
                        trrr[3]= tr[3];
                        trs.add(trrr);
                        abcSet.add(trrr[1]);
                    }
                }else{
                    String trr[] = tr[1].split("\\s*/\\s*");
                    tr[1]=trr[0];
                    try {
                        tr[2] = trr[1];
                    }catch (Exception e){
                        tr[2] = " ";
                    }
                    trs.add(tr);
                    abcSet.add(tr[1]);
                }
            }
        }

        br.close();

        List abc = new ArrayList<>(abcSet);
        Collections.sort(abc);
        Alphabet<String> alphabet = Alphabets.fromCollection(abc);
        CompactMealy<String, Word<String>> mealym = new CompactMealy<String, Word<String>>(alphabet);

        Map<String,Integer> states = new HashMap<String,Integer>();
        Integer si=null,sf=null;

        Map<String,Word<String>> words = new HashMap<String,Word<String>>();


        WordBuilder<String> aux = new WordBuilder<>();

        aux.clear();
        aux.append(OMEGA_SYMBOL);
        words.put(OMEGA_SYMBOL.toString(), aux.toWord());


        for (String[] tr : trs) {
            if(!states.containsKey(tr[0])) states.put(tr[0], mealym.addState());
            if(!states.containsKey(tr[3])) states.put(tr[3], mealym.addState());

            si = states.get(tr[0]);
            sf = states.get(tr[3]);

            if(!words.containsKey(tr[1])){
                aux.clear();
                aux.add(tr[1]);
                words.put(tr[1], aux.toWord());
            }
            if(!words.containsKey(tr[2])){
                aux.clear();
                aux.add(tr[2]);
                words.put(tr[2], aux.toWord());
            }
            mealym.addTransition(si, words.get(tr[1]).toString(), sf, words.get(tr[2]));
        }

        for (Integer st : mealym.getStates()) {
            for (String in : alphabet) {
                if(mealym.getTransition(st, in)==null){
                    mealym.addTransition(st, in, st, OMEGA_SYMBOL);
                }
            }
        }


        mealym.setInitialState(states.get("s0"));

        return mealym;
    }

    public CompactMealy<String, Word<String>> loadMealyMachine(File f) throws Exception {

        Pattern kissLine = Pattern.compile("\\s*(\\S+)\\s+--\\s+(\\S+)\\s*/\\s*(\\S+)\\s+->\\s+(\\S+)\\s*");

        BufferedReader br = new BufferedReader(new FileReader(f));

        List<String[]> trs = new ArrayList<String[]>();

        HashSet<String> abcSet = new HashSet<>();
        List<String> abc = new ArrayList<>();

        //		int count = 0;

        while(br.ready()){
            String line = br.readLine();
            Matcher m = kissLine.matcher(line);
            if(m.matches()){
                String[] tr = new String[4];
                tr[0] = m.group(1);
                tr[1] = m.group(2);
                if(!abcSet.contains(tr[1])){
                    abcSet.add(tr[1]);
                    abc.add(tr[1]);
                }
                tr[2] = m.group(3);
                tr[3] = m.group(4);
                trs.add(tr);
            }
            //			count++;
        }

        br.close();

        Collections.sort(abc);
        Alphabet<String> alphabet = Alphabets.fromCollection(abc);
        CompactMealy<String, Word<String>> mealym = new CompactMealy<String, Word<String>>(alphabet);

        Map<String,Integer> states = new HashMap<String,Integer>();
        Integer si=null,sf=null;

        Map<String,Word<String>> words = new HashMap<String,Word<String>>();


        WordBuilder<String> aux = new WordBuilder<>();

        aux.clear();
        aux.append(OMEGA_SYMBOL);
        words.put(OMEGA_SYMBOL.toString(), aux.toWord());

        Integer s0 = null;

        for (String[] tr : trs) {
            if(!states.containsKey(tr[0])) states.put(tr[0], mealym.addState());
            if(!states.containsKey(tr[3])) states.put(tr[3], mealym.addState());

            si = states.get(tr[0]);
            if(s0==null) s0 = si;
            sf = states.get(tr[3]);

            if(!words.containsKey(tr[1])){
                aux.clear();
                aux.add(tr[1]);
                words.put(tr[1], aux.toWord());
            }
            if(!words.containsKey(tr[2])){
                aux.clear();
                aux.add(tr[2]);
                words.put(tr[2], aux.toWord());
            }
            mealym.addTransition(si, words.get(tr[1]).toString(), sf, words.get(tr[2]));
        }

        for (Integer st : mealym.getStates()) {
            for (String in : alphabet) {
                if(mealym.getTransition(st, in)==null){
                    mealym.addTransition(st, in, st, OMEGA_SYMBOL);
                }
            }
        }


        mealym.setInitialState(s0);

        return mealym;
    }

    public ProductMealy loadProductMealy(File f, String dir) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(f));
        ProductMealy productMealy = null;
        int comp_num = 0;
        while (br.ready()) {
            String line = br.readLine();
            line = BenchmarksDir + dir + line;
            File file = new File(line);
            CompactMealy component;
            if (line.contains(".dot"))
                component = loadMealyMachineFromDot(file);
            else if (line.contains(".txt"))
                component = loadMealyMachine(file);
            else
                throw (new IOException("File format not supported"));

            if (productMealy == null) productMealy = new ProductMealy(component);
            else productMealy.mergeFSMs(component, 1, 30000);
            comp_num++;
        }
        productMealy.setComponents_count(comp_num);
        return productMealy;
    }

    public static void saveMealyMachineAsDot(CompactMealy<String, Word<String>> mealy, File f) throws Exception {

        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        List<Integer> states = new ArrayList<>();
        states.addAll(mealy.getStates()); states.removeAll(mealy.getInitialStates());
        states.addAll(0, mealy.getInitialStates());
        for (Integer si : states) {
            for (String in : mealy.getInputAlphabet()) {
                CompactMealyTransition<Word<String>> tr = mealy.getTransition(si,in);
                Word<String> out = tr.getOutput();
                int sj = tr.getSuccId();
                bw.append(String.format("s%d -> s%d [label=\"%s  /  %s\"];\n", si,sj,in,out));
            }
        }
        bw.close();
    }


    public static String ExtractValue(String string_1) {
        // TODO Auto-generated method stub
        int value_1 = 0;
        int j = string_1.lastIndexOf(" ");
        String string_2 = "";
        if (j >= 0) {
            string_2 = string_1.substring(j + 1);
        }
        return string_2;
    }

    public static void writeFile(String filePath, String line) throws IOException {
        File myObj = new File(filePath);
        FileWriter myWriter = new FileWriter(myObj, true);
        myWriter.write(line);
        myWriter.close();
    }
    
    public static void writeDataLineByLine(String filePath, String[] data) throws IOException {
        // first create file object for file placed at location
        // specified by filepath
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file, true);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            writer.writeNext(data);
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            throw (e);
        }
    }

    public static void writeDataHeader(String filePath, String[] data) throws IOException {
        // first create file object for file placed at location
        // specified by filepath
        File file = new File(filePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file, false);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            writer.writeNext(data);
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            throw (e);
        }
    }

    public static void printMachine(CompactMealy<String, Word<String>> machine, boolean isSclStar, FileWriter file){
        try {
            if (isSclStar) {
                file.write("SCL-Star Algorithm output:\n");
            }
        }
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        Collection<Integer> states = machine.getStates();
        Alphabet<String> alphabet = machine.getInputAlphabet();
        for(int currentState : states){
            for(String input : alphabet){
                CompactMealyTransition<Word<String>> transition =
                        machine.getTransition(currentState, input);
                Word<String> output = transition.getOutput();
                int nextState = transition.getSuccId();
                printTransition(currentState, input, output, nextState, file);
            }
        }
    }

    private static void printTransition(int currentState, String input, Word<String> output,
                                        int nextState, FileWriter file){
        try {
            String line = "\ts" + currentState + " -> " + "s" + nextState + " [label=\"" + input + "  /  " + output + "\" ];\n";
            file.write(line);
        }
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void clearFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    clearFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

}
