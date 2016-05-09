package com.Joedobo27.WUmod;

import javassist.*;
import javassist.bytecode.*;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


@SuppressWarnings({"WeakerAccess", "unused"})
public class aaaJoeCommon implements WurmServerMod, Initable{
    public static boolean modifiedCheckSaneAmounts = false;
    public static boolean overwroteForage = false;
    public static boolean overwroteHerb = false;

    private static ClassPool pool;
    public static Class forageDataClazz;
    public static Class herbDataClazz;
    private static ClassFile cfCreationEntry;
    private static ConstPool cpCreationEntry;
    private static MethodInfo checkSaneAmountsMInfo;
    private static CodeAttribute checkSaneAmountsAttribute;
    private static CodeIterator checkSaneAmountsIterator;
    private static MethodInfo CEInitMInfo;
    private static CodeAttribute CEInitAttribute;
    private static CodeIterator CEInitIterator;

    private static final Logger logger;
    public static FileHandler joeFileHandler;
    static {
        try {
            joeFileHandler = new FileHandler(
                    "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\Wurm Unlimited Dedicated Server\\mods\\aaaJoeCommon.log");
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger = Logger.getLogger(aaaJoeCommon.class.getName());
        logger.setUseParentHandlers(false);
        for (Handler a : logger.getHandlers()) {
            logger.removeHandler(a);
        }
        logger.addHandler(joeFileHandler);
        logger.setLevel(Level.ALL);
    }

    @Override
    public void init() {
        pool = HookManager.getInstance().getClassPool();
        logger.log(Level.INFO, "aaaJoeCommon loaded.");
}

    //<editor-fold desc="Javassist and bytecode altering section.">
    @Deprecated
    private static void setJSSelf() throws NotFoundException {
        CtClass ctcSelf = pool.get(aaaJoeCommon.class.getName());
    }

    private static void setJSCreationEntry() throws NotFoundException {
        CtClass ctcCreationEntry = pool.get("com.wurmonline.server.items.CreationEntry");
        cfCreationEntry = ctcCreationEntry.getClassFile();
        cpCreationEntry = cfCreationEntry.getConstPool();
    }

    @Deprecated
    private static void setJSForage() throws NotFoundException {
        CtClass ctcForage = pool.get("com.wurmonline.server.behaviours.Forage");
        CtClass ctcForageJDB = pool.get("com.Joedobo27.WUmod.ForageJDB");
        CtClass ctcForageDataJDB = pool.get("com.Joedobo27.WUmod.ForageDataJDB");
    }

    @Deprecated
    private static void setJSHerb() throws NotFoundException {
        CtClass ctcHerb = pool.get("com.wurmonline.server.behaviours.Herb");
        CtClass ctcHerbJDB = pool.get("com.Joedobo27.WUmod.HerbJDB");
        CtClass ctcHerbDataJDB = pool.get("com.Joedobo27.WUmod.HerbDataJDB");
    }

    public static void jsForageOverwrite() throws NotFoundException, CannotCompileException {
        CtClass ctcForageData = pool.getAndRename("com.Joedobo27.WUmod.ForageDataJDB", "com.wurmonline.server.behaviours.ForageData");
        ClassMap cmForageData = new ClassMap();
        cmForageData.put("com.Joedobo27.WUmod.ForageDataJDB", "com.wurmonline.server.behaviours.ForageData");
        ctcForageData.replaceClassName(cmForageData);
        ctcForageData.rebuildClassFile();

        ClassMap cmForage = new ClassMap();
        cmForage.put("com.Joedobo27.WUmod.ForageJDB", "com.wurmonline.server.behaviours.Forage");
        CtClass ctcForageNew = pool.getAndRename("com.Joedobo27.WUmod.ForageJDB", "com.wurmonline.server.behaviours.Forage");
        ctcForageNew.replaceClassName(cmForage);
        ctcForageNew.replaceClassName(cmForageData);
        ctcForageNew.rebuildClassFile();
        forageDataClazz = ctcForageData.toClass();
        overwroteForage = true;

    }

    public static void jsHerbOverwrite() throws NotFoundException, CannotCompileException {
        CtClass ctcHerbData = pool.getAndRename("com.Joedobo27.WUmod.HerbDataJDB", "com.wurmonline.server.behaviours.HerbData");
        ClassMap cmHerbData = new ClassMap();
        cmHerbData.put("com.Joedobo27.WUmod.HerbDataJDB", "com.wurmonline.server.behaviours.HerbData");
        ctcHerbData.replaceClassName(cmHerbData);
        ctcHerbData.rebuildClassFile();

        ClassMap cmHerb = new ClassMap();
        cmHerb.put("com.Joedobo27.WUmod.HerbJDB", "com.wurmonline.server.behaviours.Herb");
        CtClass ctcHerbNew = pool.getAndRename("com.Joedobo27.WUmod.HerbJDB", "com.wurmonline.server.behaviours.Herb");
        ctcHerbNew.replaceClassName(cmHerb);
        ctcHerbNew.replaceClassName(cmHerbData);
        ctcHerbNew.rebuildClassFile();
        herbDataClazz = ctcHerbData.toClass();
        overwroteHerb = true;
    }

    public static void jsCheckSaneAmountsExclusions() throws NotFoundException, CannotCompileException, FileNotFoundException,
            BadBytecode{
        JDBByteCode find;
        JDBByteCode subFind;
        JDBByteCode replace;
        String replaceResult;
        setJSCreationEntry();
        //<editor-fold desc="Change information.">
        /*
        Add this field and initialization code to the class's static initiator.
        - public static Arraylist<Integer> largeMaterialRatioDifferentials = new Arraylist(Arrays.asList(73));
        */
        //</editor-fold>

        FieldInfo f = new FieldInfo(cpCreationEntry, "largeMaterialRatioDifferentials", "Ljava/util/ArrayList;");
        f.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
        cfCreationEntry.addField(f);

        // Modify the class static initiator to initiate largeMaterialRatioDifferentials and add int for lye.
        // ******
        setCreationEntryInit(cfCreationEntry,"()V", "<clinit>");
        // insert gap for the code which will initialize the new largeMaterialRatioDifferentials field.
        CEInitIterator.insertGap(18, 25);

        // Prepare find and replace.
        find = new JDBByteCode();
        find.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.ANEWARRAY, Opcode.PUTSTATIC, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.RETURN)));
        find.setOperandStructure(new ArrayList<>(Arrays.asList(
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// class com/wurmonline/server/items/CreationRequirement"),
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Field emptyReqs:[Lcom/wurmonline/server/items/CreationRequirement;"),
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")));
        find.setOpcodeOperand();

        replace = new JDBByteCode();
        replace.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.NEW, Opcode.DUP, Opcode.ICONST_1,
                Opcode.ANEWARRAY, Opcode.DUP, Opcode.ICONST_0, Opcode.BIPUSH, Opcode.INVOKESTATIC, Opcode.AASTORE, Opcode.INVOKESTATIC,
                Opcode.INVOKESPECIAL, Opcode.PUTSTATIC, Opcode.RETURN)));
        replace.setOperandStructure(new ArrayList<>(Arrays.asList(
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// class java/util/ArrayList"),
                "", "",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// class java/lang/Integer"),
                "", "", "49",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;"),
                "",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Method java/util/Arrays.asList:([Ljava/lang/Object;)Ljava/util/List;"),
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Method java/util/ArrayList.\"<init>\":(Ljava/util/Collection;)V"),
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Field largeMaterialRatioDifferentials:Ljava/util/ArrayList;"),
                "")));
        replace.setOpcodeOperand();

        replaceResult = JDBByteCode.byteCodeFindReplace(find.getOpcodeOperand(), "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,b1",
                replace.getOpcodeOperand(), CEInitIterator, "<clinit>");
        logger.log(Level.INFO, replaceResult);
        CEInitAttribute.computeMaxStack();
        CEInitMInfo.rebuildStackMapIf6(pool, cfCreationEntry);

        //<editor-fold desc="Modify checkSaneAmounts() in CreationEntry.class.">
        /*
        Change checkSaneAmounts of CreationEntry to exclude certain items from a section that was bugging creation because of
        1) combine for all 2)large weight difference between finished item and one or both materials.
                Lines 387 - 401
                - was: if (template.isCombine() && this.objectCreated != 73)
            - becomes: if (template.isCombine() && !largeMaterialRatioDifferentials.contains(this.objectCreated)) {
            add: a public static field of ArrayList to represent largeMaterialRatioDifferentials.
        */
        //</editor-fold>
        setCheckSaneAmounts(cfCreationEntry,
                "(Lcom/wurmonline/server/items/Item;ILcom/wurmonline/server/items/Item;ILcom/wurmonline/server/items/ItemTemplate;Lcom/wurmonline/server/creatures/Creature;Z)V",
                "checkSaneAmounts");
        checkSaneAmountsIterator.insertGap(395, 7);
        find = new JDBByteCode();
        find.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.ALOAD, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.ALOAD_0, Opcode.GETFIELD, Opcode.BIPUSH, Opcode.IF_ICMPEQ)));
        find.setOperandStructure(new ArrayList<>(Arrays.asList("05",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Method com/wurmonline/server/items/ItemTemplate.isCombine:()Z"),
                "0035", "", "", "", "", "", "","","",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Field objectCreated:I"),
                "49", "0025")));
        find.setOpcodeOperand();
        replace = new JDBByteCode();
        replace.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.ALOAD, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.GETSTATIC,
                Opcode.ALOAD_0, Opcode.GETFIELD, Opcode.INVOKESTATIC, Opcode.INVOKEVIRTUAL, Opcode.IFNE)));
        replace.setOperandStructure(new ArrayList<>(Arrays.asList("05",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Method com/wurmonline/server/items/ItemTemplate.isCombine:()Z"),
                "0035",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Field largeMaterialRatioDifferentials:Ljava/util/ArrayList;"),
                "",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Field objectCreated:I"),
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;"),
                JDBByteCode.findConstantPoolReference(cpCreationEntry, "// Method java/util/ArrayList.contains:(Ljava/lang/Object;)Z"),
                "0025"
        )));
        replace.setOpcodeOperand();
        replaceResult = JDBByteCode.byteCodeFindReplace(find.getOpcodeOperand(), find.getOpcodeOperand(), replace.getOpcodeOperand(), checkSaneAmountsIterator,
                "checkSaneAmounts");
        logger.log(Level.INFO, replaceResult);
        checkSaneAmountsMInfo.rebuildStackMapIf6(pool, cfCreationEntry);
        modifiedCheckSaneAmounts = true;
    }
    //</editor-fold>

    //<editor-fold desc="Setters for CodeIterator, CodeAttribute, methodInfo.">
    private static void setCheckSaneAmounts(ClassFile cf, String desc, String name) {
        if (checkSaneAmountsMInfo == null || checkSaneAmountsIterator == null || checkSaneAmountsAttribute == null) {
            for (List a : new List[]{cf.getMethods()}){
                for(Object b : a){
                    MethodInfo MInfo = (MethodInfo) b;
                    if (Objects.equals(MInfo.getDescriptor(), desc) && Objects.equals(MInfo.getName(), name)){
                        checkSaneAmountsMInfo = MInfo;
                        break;
                    }
                }
            }
            if (checkSaneAmountsMInfo == null){
                throw new NullPointerException();
            }
            checkSaneAmountsAttribute = checkSaneAmountsMInfo.getCodeAttribute();
            checkSaneAmountsIterator = checkSaneAmountsAttribute.iterator();
        }
    }

    private static void setCreationEntryInit(ClassFile cf, String desc, String name) {
        if (CEInitMInfo == null || CEInitIterator == null || CEInitAttribute == null) {
            for (List a : new List[]{cf.getMethods()}){
                for(Object b : a){
                    MethodInfo MInfo = (MethodInfo) b;
                    if (Objects.equals(MInfo.getDescriptor(), desc) && Objects.equals(MInfo.getName(), name)){
                        CEInitMInfo = MInfo;
                        break;
                    }
                }
            }
            if (CEInitMInfo == null){
                throw new NullPointerException();
            }
            CEInitAttribute = CEInitMInfo.getCodeAttribute();
            CEInitIterator = CEInitAttribute.iterator();
        }
    }

    //</editor-fold>
}