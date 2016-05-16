package com.Joedobo27.WUmod;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Mnemonic;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "WeakerAccess"})
class JDBByteCode {

    private ArrayList<Integer> opCodeStructure;
    private ArrayList<String > operandStructure;
    private String opcodeOperand;
    private static final String EMPTY_STRING = "";
    private static final int EMPTY_INT = Integer.MAX_VALUE;

    @SuppressWarnings("FieldCanBeLocal")
    private static Logger logger;
    static {
        logger = Logger.getLogger(JDBByteCode.class.getName());
        logger.setUseParentHandlers(false);
        for (Handler a : logger.getHandlers()) {
            logger.removeHandler(a);
        }
        logger.addHandler(aaaJoeCommon.joeFileHandler);
        logger.setLevel(Level.FINE);
    }

    /**
     * This method combines the values in opCodeStructure and operandStructure for its instance and sets the new value.
     */
    public void setOpcodeOperand() {
        String s = "";
        s = s.concat(String.format("%02X", this.opCodeStructure.get(0) & 0xff));
        s = s.concat(this.operandStructure.get(0));
        for (int i = 1; i < this.opCodeStructure.size(); i++) {
            s = s.concat(",");
            s = s.concat(String.format("%02X", this.opCodeStructure.get(i) & 0xff));
            s = s.concat(this.operandStructure.get(i));
        }
        this.opcodeOperand = s;
    }

    public void setOperandStructure(ArrayList<String> operandStructure) {
        this.operandStructure = operandStructure;
    }

    public void setOpCodeStructure(ArrayList<Integer> opCodeStructure) {
        this.opCodeStructure = opCodeStructure;
    }

    public String getOpcodeOperand() {return this.opcodeOperand;}


    //// Method com/wurmonline/server/items/Item.getWeightGrams:()I
    public static String addConstantPoolReference(ConstPool cp, String javapDesc) {
        String[] splitDesc1;
        String[] splitDesc2;
        //String refType = "";
        //String classInfo = "";
        String name = "";
        String descriptor = "";
        int classConstPoolIndex = Integer.MAX_VALUE;
        int poolIndex = Integer.MAX_VALUE;


        splitDesc1 = javapDesc.split("[ .:]");
        if (Objects.equals(splitDesc1[1], "String")) {
            splitDesc2 = javapDesc.split("String ");
            splitDesc1 = new String[]{"//", "String", splitDesc2[1]};
        }

        if (splitDesc1.length < 3 || splitDesc1.length > 5)
            throw new UnsupportedOperationException();
        if ( (Objects.equals(splitDesc1[1], "Method") || Objects.equals(splitDesc1[1], "Field")) && splitDesc1.length == 3) {
            throw new UnsupportedOperationException();
        }

        if ( (Objects.equals(splitDesc1[1], "Method") || Objects.equals(splitDesc1[1], "Field")) && splitDesc1.length == 4) {
            // The class reference is missing. This happens when a field or method is defined in the same class and javap
            // assumes the reader is aware. addFieldrefInfo and addMethodrefInfo need specifics not assumptions.
            try {
                classConstPoolIndex = Integer.valueOf(findConstantPoolReference(cp, "// class " + cp.getClassName().replaceAll("\\.", "/")), 16);
            }catch (UnsupportedOperationException e) {
                classConstPoolIndex = cp.addClassInfo(cp.getClassName().replaceAll("/", "."));
            }
            name = splitDesc1[2];
            name = name.replaceAll("\"", "");
            descriptor = splitDesc1[3];
        }
        if ( (Objects.equals(splitDesc1[1], "Method") || Objects.equals(splitDesc1[1], "Field")) && splitDesc1.length == 5) {
            try {
                classConstPoolIndex = Integer.valueOf(findConstantPoolReference(cp, "// class " + splitDesc1[2].replaceAll("\\.", "/")), 16);
            }catch (UnsupportedOperationException e) {
                classConstPoolIndex = cp.addClassInfo(splitDesc1[2].replaceAll("/", "."));
            }
            name = splitDesc1[3];
            name = name.replaceAll("\"", "");
            descriptor = splitDesc1[4];
        }
        switch (splitDesc1[1]){
            case "Method":
                poolIndex = cp.addMethodrefInfo(classConstPoolIndex, name, descriptor);
                break;
            case "Field":
                poolIndex = cp.addFieldrefInfo(classConstPoolIndex, name, descriptor);
                break;
            case "class":
                try {
                    poolIndex = Integer.valueOf(findConstantPoolReference(cp, "// class " + splitDesc1[2].replaceAll("\\.", "/")), 16);
                }catch (UnsupportedOperationException e) {
                    poolIndex = cp.addClassInfo(splitDesc1[2].replaceAll("/", "."));
                }
                break;
            case "String":
                poolIndex = cp.addStringInfo(splitDesc1[2]);
        }
        if (Objects.equals(poolIndex, EMPTY_INT))
            throw new UnsupportedOperationException();

        return String.format("%04X", poolIndex & 0xffff);
    }

    /**
     * This method looks for matches in the constantPool and returns found addresses.
     *
     * @param cp is type ConstPool. This object is for accessing a specific ConstPool.
     * @param javapDesc is a string. This string data is copied directly from javap.exe data dump and represents a description
     *                  of its explanatory value in the constantPool.
     * @return is a string of 4 digits. These 4 numbers(in string form) are the address in the ConstantPool
     */
    public static String findConstantPoolReference(ConstPool cp, String javapDesc) {
        String[] splitDesc1;
        String[] splitDesc2;
        String refClass = "";
        String cpClass;
        String name = "";
        String eqResult;
        String descriptor = "";
        int classConstPoolIndex = Integer.MAX_VALUE;
        int poolIndex = Integer.MAX_VALUE;
        String toReturn = "";


        splitDesc1 = javapDesc.split("[ .:]");
        if (Objects.equals(splitDesc1[1], "String")) {
            splitDesc2 = javapDesc.split("String ");
            splitDesc1 = new String[]{"//", "String", splitDesc2[1]};
        }
        if (Objects.equals(splitDesc1[1], "float")) {
            splitDesc2 = javapDesc.split("float ");
            splitDesc1 = new String[]{"//", "float", splitDesc2[1]};
        }

        if (splitDesc1.length < 3 || splitDesc1.length > 5)
            throw new UnsupportedOperationException();
        if ( (Objects.equals(splitDesc1[1], "Method") || Objects.equals(splitDesc1[1], "Field")) && splitDesc1.length == 3) {
            throw new UnsupportedOperationException();
        }

        if ( (Objects.equals(splitDesc1[1], "Method") || Objects.equals(splitDesc1[1], "Field") || Objects.equals(splitDesc1[1], "InterfaceMethod"))
                && splitDesc1.length == 4) {
            // The class reference is missing. This happens when a field or method is defined in the same class and javap
            // assumes the reader is aware. ConstPool references still show the class reference even if the lines in methods don't.
            //classConstPoolIndex = cp.addClassInfo(cp.getClassName().replaceAll("/", "."));
            refClass = cp.getClassName();
            refClass = refClass.replace("/", ".");
            name = splitDesc1[2];
            name = name.replaceAll("\"", "");
            descriptor = splitDesc1[3];
        }
        if ( (Objects.equals(splitDesc1[1], "Method") || Objects.equals(splitDesc1[1], "Field") || Objects.equals(splitDesc1[1], "InterfaceMethod"))
                && splitDesc1.length == 5) {
            //classConstPoolIndex = cp.addClassInfo(splitDesc1[2]);
            refClass = splitDesc1[2];
            refClass = refClass.replace("/", ".");
            name = splitDesc1[3];
            name = name.replaceAll("\"", "");
            descriptor = splitDesc1[4];
        }
        for (int i = 1; i < cp.getSize(); i++) {
            try {
                switch (splitDesc1[1]) {
                    case "Method":
                        eqResult = cp.eqMember(name, descriptor, i);
                        cpClass = cp.getMethodrefClassName(i);
                        if (eqResult != null && Objects.equals(refClass, cpClass))
                            toReturn = String.format("%04X", i & 0xffff);
                        break;
                    case "String":
                        String cpStr = cp.getStringInfo(i);
                        String refStr = splitDesc1[2];
                        if (cpStr != null && Objects.equals(cpStr, refStr))
                            toReturn = String.format("%04X", i & 0xffff);
                        break;
                    case "Field":
                        eqResult = cp.eqMember(name, descriptor, i);
                        cpClass = cp.getMethodrefClassName(i);
                        if (eqResult != null && Objects.equals(refClass, cpClass))
                            toReturn = String.format("%04X", i & 0xffff);
                        break;
                    case "class":
                        refClass = splitDesc1[2];
                        refClass = refClass.replace("/", ".");
                        cpClass = cp.getClassInfo(i);
                        if (cpClass != null && Objects.equals(cpClass, refClass))
                            toReturn = String.format("%04X", i & 0xffff);
                        break;
                    case "long":
                        long cpLong = cp.getLongInfo(i);
                        long refLong = Long.parseLong(splitDesc1[2].replace("l", ""), 10);
                        if (Objects.equals(cpLong, refLong))
                            toReturn = String.format("%04X", i & 0xffff);
                        break;
                    case "float":
                        float cpFloat = cp.getFloatInfo(i);
                        float refFloat = Float.parseFloat(splitDesc1[2].replace("f", ""));
                        if (Objects.equals(cpFloat, refFloat))
                            toReturn = String.format("%04X", i & 0xffff);
                        break;
                    case "InterfaceMethod":
                        eqResult = cp.eqMember(name, descriptor, i);
                        cpClass = cp.getInterfaceMethodrefClassName(i);
                        if (eqResult != null && Objects.equals(refClass, cpClass))
                            toReturn = String.format("%04X", i & 0xffff);
                        break;
                }
            } catch (ClassCastException e) {
                // This method does ConstPool information fetching that throws this exception often, ignore it.
            }
            if (!Objects.equals(toReturn, EMPTY_STRING)) {
                break;
            }
        }
        if (Objects.equals(toReturn, EMPTY_STRING)) {
            throw new UnsupportedOperationException();
        }
        return toReturn;
    }

    /**
     * Break up a string which is a list of hexadecimal digits into a ArrayList of HashMap-str,int.
     * Each hexidecimal 2 digit entry has a string name: opCode, opOperand1, opOperand2 ... etc.
     *
     * AC,1064,3608 would be ["opCode":0xAC, "opCode":0x10 "opOperand1":0x64, "opCode":0x36 "opOperand1":0x08].
     *
     * @param str A string representations of hexadecimal digits. Commas delimit ArrayList entries.
     * @return Return an ArrayList of HashMaps.
     */
    private static ArrayList<HashMap<String, Integer>> stringToArrayList(String str){
        ArrayList<HashMap<String, Integer>> ab = new ArrayList<>();
        HashMap<String, Integer> cd = new HashMap<>(4);
        for (String split : str.split(",")){
            switch (split.length()/2){
                case 1 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    break;
                case 2 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    cd.put("opOperand1", Integer.parseInt(split.substring(2), 16));
                    break;
                case 3 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    cd.put("opOperand1", Integer.parseInt(split.substring(2, 4), 16));
                    cd.put("opOperand2", Integer.parseInt(split.substring(4), 16));
                    break;
                case 4 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    cd.put("opOperand1", Integer.parseInt(split.substring(2, 4), 16));
                    cd.put("opOperand2", Integer.parseInt(split.substring(4, 6), 16));
                    cd.put("opOperand3", Integer.parseInt(split.substring(6), 16));
                    break;
                case 5 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    cd.put("opOperand1", Integer.parseInt(split.substring(2, 4), 16));
                    cd.put("opOperand2", Integer.parseInt(split.substring(4, 6), 16));
                    cd.put("opOperand3", Integer.parseInt(split.substring(6, 8), 16));
                    cd.put("opOperand4", Integer.parseInt(split.substring(8), 16));
                    break;
                case 6 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    cd.put("opOperand1", Integer.parseInt(split.substring(2, 4), 16));
                    cd.put("opOperand2", Integer.parseInt(split.substring(4, 6), 16));
                    cd.put("opOperand3", Integer.parseInt(split.substring(6, 8), 16));
                    cd.put("opOperand4", Integer.parseInt(split.substring(8, 10), 16));
                    cd.put("opOperand5", Integer.parseInt(split.substring(10), 16));
                    break;
                case 7 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    cd.put("opOperand1", Integer.parseInt(split.substring(2, 4), 16));
                    cd.put("opOperand2", Integer.parseInt(split.substring(4, 6), 16));
                    cd.put("opOperand3", Integer.parseInt(split.substring(6, 8), 16));
                    cd.put("opOperand4", Integer.parseInt(split.substring(8, 10), 16));
                    cd.put("opOperand5", Integer.parseInt(split.substring(10, 12), 16));
                    cd.put("opOperand6", Integer.parseInt(split.substring(12), 16));
                    break;
                case 8 :
                    cd.put("opCode", Integer.parseInt(split.substring(0, 2), 16));
                    cd.put("opOperand1", Integer.parseInt(split.substring(2, 4), 16));
                    cd.put("opOperand2", Integer.parseInt(split.substring(4, 6), 16));
                    cd.put("opOperand3", Integer.parseInt(split.substring(6, 8), 16));
                    cd.put("opOperand4", Integer.parseInt(split.substring(8, 10), 16));
                    cd.put("opOperand5", Integer.parseInt(split.substring(10, 12), 16));
                    cd.put("opOperand6", Integer.parseInt(split.substring(12, 14), 16));
                    cd.put("opOperand7", Integer.parseInt(split.substring(14), 16));
                    break;
            }
            ab.add(new HashMap<>(cd));
            cd.clear();
        }
        return ab;
    }

    /**
     * This method constructs a byte[] that can be used in a CodeIterator's write method.
     *
     * @param replace String, Hexadecimal values as strings.
     * @return  Return a byte[] equivalent of replace.
     */
    private static byte[] makeReplaceArray(String replace){
        ArrayList<int[]> replaceFinal = new ArrayList<>();
        String[] replaceSplit;

        replaceSplit = replace.split(",");
        byte[] a = new byte[(replace.length() - (replaceSplit.length - 1)) / 2];
        int aIndex = 0;
        for (String b:replaceSplit){
            for (int i=0; i < b.length()/2; i++){
                int c = Short.valueOf(b.substring(i*2,(i*2)+2), 16);
                a[aIndex] = (byte) c; // Intentionally doing a narrowing conversion. The two hex entries are all that matter.
                // and whether 0xff is 255 or -1 doesn't matter.
                aIndex++;
            }
        }

        return a;
    }

    /**
     * This method is used to find and replace byte code which the CodeIterator ci represents. To use this tool specify hexadecimal values
     * (as a string) that must be found in byte code. The "subFind" string of hexadecimal values is a subset of the find value and represent
     * what values will be replace. Finally, "replace" is a string of hexadecimal values that will be substituted in place of "subFind".
     * All the hexadecimal value-strings are coma delimited to indicate byte code indexing.
     *
     * @param find String, This arg is many hexadecimal values in string form, its length must be evenly divisible by 2,
     *             and commas delimit byteCode indexing.
     * @param subFind String, This arg is many hexadecimal values in string form, it must be a subset of find
     *                and is formatted like find.
     * @param replace String, This arg is many hexadecimal values in string form. These are the byte that will be written in place of find.
     *                The number or length of non-comma characters must match subFind's length.
     * @param ci CodeIterator, This object represents the method which will be changed.
     * @param method String, The name of the target method which will be changed.
     */
    public static String byteCodeFindReplace(String find, String subFind, String replace, CodeIterator ci, String method) throws BadBytecode {
        int findSize;
        int subSize;
        int replacedIndexLines = 0;
        String toReturn = "NOTHING found or replaced in " + method + ".";
        LinkedList<HashMap<String, Integer>> byteCodeLinesSub = new LinkedList<>();
        ArrayList<HashMap<String, Integer>> matchedEntries = new ArrayList<>();

        // Convert string references to ArrayLists.
        ArrayList<HashMap<String, Integer>> findCodes = stringToArrayList(find);
        ArrayList<HashMap<String, Integer>> subFindCodes = stringToArrayList(subFind);
        byte[] replaceL = makeReplaceArray(replace);
        findSize = findCodes.size();
        subSize = subFindCodes.size();

        // Look through the method for matches and replace ArrayList entries as matched.
        ArrayList<HashMap<String, Integer>> byteCodeLines = byteCodeMakeArray(ci);
        for ( int x = 0; x < byteCodeLines.size(); x++) {
            byteCodeLinesSub.add(new HashMap<>(byteCodeLines.get(x)));
            // remove index 0 from byteCodeLinesSub if its length is > FindEntries.
            if (byteCodeLinesSub.size() >= findSize) {
                if (byteCodeLinesSub.size() > findSize) {
                    byteCodeLinesSub.remove(0);
                }
                if (checkArrayEquality(byteCodeLinesSub, findCodes)){
                    int x2 = 0;
                    for (int x1 = 0; x1 < findSize + (findSize - subSize + 2); x1++) {
                        if (checkHashMapEquality(byteCodeLines.get(x + x1 - findSize + 1), subFindCodes.get(x2))) {
                            matchedEntries.add(byteCodeLines.get(x + x1 - findSize + 1));
                            x2++;
                            if (matchedEntries.size() >= subSize){
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (matchedEntries.size() > 0) {
            if (replaceL.length != (subFind.length() - (subFindCodes.size() - 1)) / 2 ){
                throw new IndexOutOfBoundsException();
            }
            ci.write(replaceL, matchedEntries.get(0).get("index"));
            toReturn = "Found and replaced bytecode in " + method + ".";
        }
        return toReturn;
    }

    /**
     * This method checks if two hashMaps are equal - HashMap.equal(HashMap). Particular to this application is that a hashMap entry, "index",
     * must be removed from one to do the comparison. The "index" value needs to be kept so a deep copy is used.
     *
     * @param removeIndex This hashMap<String, Integer> is deep copied, "index" removed, and compared to find.
     * @param findCodes This hashMap<String, Integer>  is simply compared.
     * @return boolean, If true they value equal, if false they don't value equal.
     */
    private static boolean checkHashMapEquality(HashMap<String, Integer> removeIndex, HashMap<String, Integer> findCodes){
        HashMap<String, Integer> copy;
        //noinspection unchecked
        copy = (HashMap<String, Integer>) removeIndex.clone();
        copy.remove("index");
        return copy.equals(findCodes);
    }

    /**
     * This method checks if a LinkedList and a ArrayList are equal. The variable removeIndex is deep copied and the hashMap
     * values with key "index" are removed from the copy. The "index" entry must be removed to compare the two lists. The index
     * value is used elsewhere so a deep copy is used to preserve the object.
     *
     * @param removeIndex This LinkedList<HashMap<String, Integer>>
     * @param findCodes This ArrayList<HashMap<String, Integer>>
     * @return boolean, If true they equal, if false they don't value equal.
     */
    private static boolean checkArrayEquality(LinkedList<HashMap<String, Integer>> removeIndex, ArrayList<HashMap<String, Integer>> findCodes){
        ArrayList<HashMap<String, Integer>> copyArr =  new ArrayList<>();
        //noinspection Convert2streamapi
        for (HashMap<String, Integer> itr : removeIndex){
            //noinspection unchecked
            copyArr.add((HashMap<String, Integer>) itr.clone());
        }
        for (HashMap<String, Integer> itr : copyArr){
            itr.remove("index");
        }
        int i;
        for (i=0; i < copyArr.size(); i++) {
            if (!copyArr.get(i).equals(findCodes.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Disassemble byteCode of the method represented by the CodeIterator ci and order the dissection in a nested
     * collection of ArrayList by ArrayList.
     *
     * Each Child ArrayList element is ByteCode entries ordered as:
     *          [ByteCodes index "stackIndex"(int as string), opCode "opCode"(hex as string),
     *          optionally opOperand1...8 "opOperand1...8"(hex as string)]
     *
     * @param ci CodeIterator
     * @return      An ArrayList<ArrayList<HashMap<String, Integer>> object.
     */
    private static ArrayList<HashMap<String, Integer>> byteCodeMakeArray(CodeIterator ci) throws BadBytecode{
        int index;
        int bitLine;
        int bitLine2;
        ArrayList<HashMap<String, Integer>> byteCodeLines = new ArrayList<>(500);
        Map<String, Integer> lineSub = new HashMap<>(9);
        ci.begin();
        while (ci.hasNext()) {
            lineSub.clear();
            index = ci.next();
            lineSub.put("index", index);
            switch (ci.lookAhead() - index){
                case 1:
                    lineSub.put("opCode", ci.byteAt(index));
                    break;
                case 2:
                    bitLine = ci.s16bitAt(index);
                    lineSub.put("opCode", (bitLine & 0xff00) >>> 8);
                    lineSub.put("opOperand1", (bitLine & 0x00ff));
                    break;
                case 3:
                    bitLine = ci.s32bitAt(index);
                    lineSub.put("opCode", (bitLine & 0xff000000) >>> 24);
                    lineSub.put("opOperand1", (bitLine & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand2", (bitLine & 0x0000ff00) >>> 8);
                    break;
                case 4:
                    bitLine = ci.s32bitAt(index);
                    lineSub.put("opCode", (bitLine & 0xff000000) >>> 24);
                    lineSub.put("opOperand1", (bitLine & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand2", (bitLine & 0x0000ff00) >>> 8);
                    lineSub.put("opOperand3", (bitLine & 0x000000ff));
                    break;
                case 5:
                    bitLine = ci.s32bitAt(index);
                    lineSub.put("opCode", (bitLine & 0xff000000) >>> 24);
                    lineSub.put("opOperand1", (bitLine & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand2", (bitLine & 0x0000ff00) >>> 8);
                    lineSub.put("opOperand3", (bitLine & 0x000000ff));
                    bitLine2 = ci.byteAt(index + 4);
                    lineSub.put("opOperand4", bitLine2);
                    break;
                case 6:
                    bitLine = ci.s32bitAt(index);
                    lineSub.put("opCode", (bitLine & 0xff000000) >>> 24);
                    lineSub.put("opOperand1", (bitLine & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand2", (bitLine & 0x0000ff00) >>> 8);
                    lineSub.put("opOperand3", (bitLine & 0x000000ff));
                    bitLine2 = ci.s16bitAt(index + 4);
                    lineSub.put("opOperand4", (bitLine2 & 0xff00) >>> 8);
                    lineSub.put("opOperand5", (bitLine2 & 0x00ff));
                    break;
                case 7:
                    bitLine = ci.s32bitAt(index);
                    lineSub.put("opCode", (bitLine & 0xff000000) >>> 24);
                    lineSub.put("opOperand1", (bitLine & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand2", (bitLine & 0x0000ff00) >>> 8);
                    lineSub.put("opOperand3", (bitLine & 0x000000ff));
                    bitLine2 = ci.s32bitAt(index + 4);
                    lineSub.put("opOperand4", (bitLine2 & 0xff000000) >>> 24);
                    lineSub.put("opOperand5", (bitLine2 & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand6", (bitLine2 & 0x0000ff00) >>> 8);
                    break;
                case 8:
                    bitLine = ci.s32bitAt(index);
                    lineSub.put("opCode", (bitLine & 0xff000000) >>> 24);
                    lineSub.put("opOperand1", (bitLine & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand2", (bitLine & 0x0000ff00) >>> 8);
                    lineSub.put("opOperand3", (bitLine & 0x000000ff));
                    bitLine2 = ci.s32bitAt(index + 4);
                    lineSub.put("opOperand4", (bitLine2 & 0xff000000) >>> 24);
                    lineSub.put("opOperand5", (bitLine2 & 0x00ff0000) >>> 16);
                    lineSub.put("opOperand6", (bitLine2 & 0x0000ff00) >>> 8);
                    lineSub.put("opOperand7", (bitLine2 & 0x000000ff));
                    break;
            }
            byteCodeLines.add(new HashMap<>(lineSub));
        }
        return byteCodeLines;
    }

    /**
     * This makes a file listing information about the method referred to in CodeIterator ci. The byte code is raw
     * hexadecimal values. It will differ from Javap.exe in that it doesn't take into consideration line nubers for
     * branches like javap.
     *
     * The file is named starting with the string passed to it in methodA followed by "byte code".txt
     * Information included:
     *      A JVM byte code listing in format:
     *      Index(int): # opCode: "Mnemonic" #(hex) [#-opOperand(hex)].
     *
     * @param ci CodeIterator type, This is the codeIterator for the method being printed.
     * @param method String type, This is the name of method being printed.
     * @param destinationPath String type, This is the path to where the file should be created.
     * @return String type and it's a simple success message.
     */
    public static String byteCodePrint(CodeIterator ci, String method, String destinationPath) throws FileNotFoundException, BadBytecode {

        Path printPath = Paths.get(destinationPath, method + " byte code.txt");
        PrintWriter out = new PrintWriter(printPath.toFile());

        ArrayList<HashMap<String, Integer>> mappings = byteCodeMakeArray(ci);
        for (HashMap<String, Integer> byteCode : mappings) {
            //list = 0 is index, 1 is opCode, 2 is opOperand
            if (byteCode.containsKey("opOperand7")) {
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode"))
                        + " " + Integer.toHexString(byteCode.get("opOperand1")) + " " + Integer.toHexString(byteCode.get("opOperand2"))
                        + " " + Integer.toHexString(byteCode.get("opOperand3")) + " " + Integer.toHexString(byteCode.get("opOperand4"))
                        + " " + Integer.toHexString(byteCode.get("opOperand5")) + " " + Integer.toHexString(byteCode.get("opOperand6"))
                        + " " + Integer.toHexString(byteCode.get("opOperand7")));
            } else if (byteCode.containsKey("opOperand6")) {
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode"))
                        + " " + Integer.toHexString(byteCode.get("opOperand1")) + " " + Integer.toHexString(byteCode.get("opOperand2"))
                        + " " + Integer.toHexString(byteCode.get("opOperand3")) + " " + Integer.toHexString(byteCode.get("opOperand4"))
                        + " " + Integer.toHexString(byteCode.get("opOperand5")) + " " + Integer.toHexString(byteCode.get("opOperand6")));
            } else if (byteCode.containsKey("opOperand5")) {
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode"))
                        + " " + Integer.toHexString(byteCode.get("opOperand1")) + " " + Integer.toHexString(byteCode.get("opOperand2"))
                        + " " + Integer.toHexString(byteCode.get("opOperand3")) + " " + Integer.toHexString(byteCode.get("opOperand4"))
                        + " " + Integer.toHexString(byteCode.get("opOperand5")));
            } else if (byteCode.containsKey("opOperand4")) {
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode"))
                        + " " + Integer.toHexString(byteCode.get("opOperand1")) + " " + Integer.toHexString(byteCode.get("opOperand2"))
                        + " " + Integer.toHexString(byteCode.get("opOperand3")) + " " + Integer.toHexString(byteCode.get("opOperand4")));
            } else if (byteCode.containsKey("opOperand3")) {
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode"))
                        + " " + Integer.toHexString(byteCode.get("opOperand1")) + " " + Integer.toHexString(byteCode.get("opOperand2"))
                        + " " + Integer.toHexString(byteCode.get("opOperand3")));
            } else if (byteCode.containsKey("opOperand2")) {
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode"))
                        + " " + Integer.toHexString(byteCode.get("opOperand1")) + " " + Integer.toHexString(byteCode.get("opOperand2")));
            } else if (byteCode.containsKey("opOperand1")) {
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode"))
                        + " " + Integer.toHexString(byteCode.get("opOperand1")));
            } else if (byteCode.containsKey("opCode")){
                out.println("Index: " + byteCode.get("index") + " opCode: " +
                        Mnemonic.OPCODE[byteCode.get("opCode")] + " " + Integer.toHexString(byteCode.get("opCode")));
            }

        }
        out.close();
        return method + " printing complete.";
    }

    public static void constantPoolPrint(ConstPool cp, String clazz, String destinationPath) throws FileNotFoundException {
        Path printPath = Paths.get(destinationPath, clazz + " constant pool.txt");
        PrintWriter out = new PrintWriter(printPath.toFile());
        cp.print(out);
        out.close();
    }

    /**
     * This method is used to check if the JVM code matches a hash. It's primary purpose is to detected changes in WU
     * vanilla code. When Javassist is used to replace an entire method body there is nothing that informs a mod author
     * the code should be reviewed.
     *
     * @param ci type CodeIterator
     * @return a hash value.
     * @throws BadBytecode
     */
    public static int byteCodeHashCheck(CodeIterator ci) throws BadBytecode {
        String s = byteCodeMakeArray(ci).toString();
        char[] ch = s.toCharArray();
        return Arrays.hashCode(ch);
    }
}
