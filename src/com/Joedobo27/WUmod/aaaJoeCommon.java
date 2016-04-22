package com.Joedobo27.WUmod;

import com.wurmonline.server.items.CreationEntry;
import javassist.*;
import javassist.bytecode.*;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"unused", "WeakerAccess", "FieldCanBeLocal"})
public class aaaJoeCommon implements WurmMod, Initable, ServerStartedListener {
    public static boolean modifiedCheckSaneAmounts = false;
    public static boolean overwroteForage = false;
    public static boolean overwroteHerb = false;

    private static Logger logger = Logger.getLogger(aaaJoeCommon.class.getName());

    private static ClassPool pool;
    private static CtClass ctcSelf;
    public static Class forageDataClazz;
    public static Class herbDataClazz;
    private static CtClass ctcCreationEntry;
    private static ClassFile cfCreationEntry;
    private static ConstPool cpCreationEntry;
    private static MethodInfo checkSaneAmountsMInfo;
    private static CodeAttribute checkSaneAmountsAttribute;
    private static CodeIterator checkSaneAmountsIterator;
    private static MethodInfo CEInitMInfo;
    private static CodeAttribute CEInitAttribute;
    private static CodeIterator CEInitIterator;

    @Override
    public void onServerStarted() {
    }

    @Override
    public void init() {
        pool = HookManager.getInstance().getClassPool();
        logger.log(Level.INFO, "aaaJoeCommon loaded.");
    }

    //<editor-fold desc="Javassist and bytecode altering section.">
    private static void setJSSelf() throws NotFoundException {
        ctcSelf = pool.get(com.Joedobo27.WUmod.aaaJoeCommon.class.getName());
    }

    private static void setJSCreationEntry() throws NotFoundException {
        ctcCreationEntry = pool.get("com.wurmonline.server.items.CreationEntry");
        cfCreationEntry = ctcCreationEntry.getClassFile();
        cpCreationEntry = cfCreationEntry.getConstPool();
    }

    private static void setJSForage() throws NotFoundException {
        CtClass ctcForage = pool.get("com.wurmonline.server.behaviours.Forage");
        CtClass ctcForageJDB = pool.get("com.Joedobo27.WUmod.ForageJDB");
        CtClass ctcForageDataJDB = pool.get("com.Joedobo27.WUmod.ForageDataJDB");
    }

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

        //ctcForageData.writeFile();
        //ctcForageNew.writeFile();
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

        //ctcHerbData.writeFile();
        //ctcHerbNew.writeFile();
        herbDataClazz = ctcHerbData.toClass();
        overwroteHerb = true;
    }

    public static void jsCheckSaneAmountsExclusions() throws NotFoundException, CannotCompileException, FileNotFoundException,
            BadBytecode{
        JDBByteCode jbt;
        JDBByteCode jbt1;
        String replaceResult;
        setJSCreationEntry();
        setJSSelf();
        //<editor-fold desc="Change information.">
        /*
        Change checkSaneAmounts of CreationEntry to exclude certain items from a section that was bugging creation because of
        1) combine for all 2)large weight difference between finished item and one or both materials.
        Lines 387 - 401
        - was: if (template.isCombine() && this.objectCreated != 73)
        - becomes: if (template.isCombine() && !largeMaterialRatioDifferentials.contains(this.objectCreated)) {
          add: a public static field of ArrayList to represent largeMaterialRatioDifferentials. Initialize the field with just
          ItemList.lye and use reflection to add other items relevant to the mod.

        Add this field and initialization code.
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

        // get ConstPool indexes for classes.
        int creationEntryCPIndex = cpCreationEntry.addClassInfo("com/wurmonline/server/items/CreationEntry");
        int intCPIndex = cpCreationEntry.addClassInfo("java/lang/Integer");
        int arrayListCPIndex = cpCreationEntry.addClassInfo("java/util/ArrayList");
        int arraysCPIndex = cpCreationEntry.addClassInfo("java/util/Arrays");
        // Prepare find and replace.
        jbt = new JDBByteCode();
        jbt.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.ANEWARRAY, Opcode.PUTSTATIC, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.RETURN)));
        jbt.setOperandStructure(new ArrayList<>(Arrays.asList(
                String.format("%04X", cpCreationEntry.addClassInfo("com/wurmonline/server/items/CreationRequirement") & 0xffff),
                String.format("%04X", cpCreationEntry.addFieldrefInfo(creationEntryCPIndex, "emptyReqs", "[Lcom/wurmonline/server/items/CreationRequirement;") & 0xffff),
                "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")));
        jbt.setOpcodeOperand();

        jbt1 = new JDBByteCode();
        jbt1.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.NEW, Opcode.DUP, Opcode.ICONST_1,
                Opcode.ANEWARRAY, Opcode.DUP, Opcode.ICONST_0, Opcode.BIPUSH, Opcode.INVOKESTATIC, Opcode.AASTORE, Opcode.INVOKESTATIC,
                Opcode.INVOKESPECIAL, Opcode.PUTSTATIC, Opcode.RETURN)));
        jbt1.setOperandStructure(new ArrayList<>(Arrays.asList(
                String.format("%04X", cpCreationEntry.addClassInfo("java/util/ArrayList") & 0xffff),
                "", "",
                String.format("%04X", cpCreationEntry.addClassInfo("java/lang/Integer")),
                "", "", "49",
                String.format("%04X", cpCreationEntry.addMethodrefInfo(intCPIndex, "valueOf", "(I)Ljava/lang/Integer;")),
                "",
                String.format("%04X", cpCreationEntry.addMethodrefInfo(arraysCPIndex,"asList", "([Ljava/lang/Object;)Ljava/util/List;")),
                String.format("%04X", cpCreationEntry.addMethodrefInfo(arrayListCPIndex, "<init>", "(Ljava/util/Collection;)V")),
                String.format("%04X", cpCreationEntry.addFieldrefInfo(creationEntryCPIndex, "largeMaterialRatioDifferentials", "Ljava/util/ArrayList;")),
                "")));
        jbt1.setOpcodeOperand();

        replaceResult = JDBByteCode.byteCodeFindReplace(jbt.getOpcodeOperand(), "00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,00,b1",
                jbt1.getOpcodeOperand(), CEInitIterator, "<clinit>");
        logger.log(Level.INFO, replaceResult);
        CEInitAttribute.computeMaxStack();
        CEInitMInfo.rebuildStackMapIf6(pool, cfCreationEntry);

        // Modify checkSaneAmounts() in CreationEntry.class
        // ******
        setCheckSaneAmounts(cfCreationEntry,
                "(Lcom/wurmonline/server/items/Item;ILcom/wurmonline/server/items/Item;ILcom/wurmonline/server/items/ItemTemplate;Lcom/wurmonline/server/creatures/Creature;Z)V",
                "checkSaneAmounts");
        checkSaneAmountsIterator.insertGap(395, 7);
        jbt = new JDBByteCode();
        jbt.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.ALOAD, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.NOP, Opcode.NOP, Opcode.NOP,
                Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.NOP, Opcode.ALOAD_0, Opcode.GETFIELD, Opcode.BIPUSH, Opcode.IF_ICMPEQ)));
        jbt.setOperandStructure(new ArrayList<>(Arrays.asList("05",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, ConstPool.CONST_Methodref, "com/wurmonline/server/items/ItemTemplate.isCombine:()Z"),
                "0035", "", "", "", "", "", "","","",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, ConstPool.CONST_Fieldref, "objectCreated:I"),
                "49", "0025")));
        jbt.setOpcodeOperand();
        jbt1 = new JDBByteCode();
        jbt1.setOpCodeStructure(new ArrayList<>(Arrays.asList(Opcode.ALOAD, Opcode.INVOKEVIRTUAL, Opcode.IFEQ, Opcode.GETSTATIC,
                Opcode.ALOAD_0, Opcode.GETFIELD, Opcode.INVOKESTATIC, Opcode.INVOKEVIRTUAL, Opcode.IFNE)));
        jbt1.setOperandStructure(new ArrayList<>(Arrays.asList("05",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, ConstPool.CONST_Methodref, "com/wurmonline/server/items/ItemTemplate.isCombine:()Z"),
                "0035",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, ConstPool.CONST_Fieldref, "largeMaterialRatioDifferentials:Ljava/util/ArrayList;"),
                "",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, ConstPool.CONST_Fieldref, "objectCreated:I"),
                String.format("%04X", cpCreationEntry.addMethodrefInfo(intCPIndex, "valueOf", "(I)Ljava/lang/Integer;") & 0xffff),
                String.format("%04X", cpCreationEntry.addMethodrefInfo(arrayListCPIndex, "contains", "(Ljava/lang/Object;)Z") & 0xffff),
                "0025"
                )));
        jbt1.setOpcodeOperand();
        replaceResult = JDBByteCode.byteCodeFindReplace(jbt.getOpcodeOperand(), jbt.getOpcodeOperand(), jbt1.getOpcodeOperand(), checkSaneAmountsIterator,
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

    private static void  setCreationEntryInit(ClassFile cf, String desc, String name) {
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