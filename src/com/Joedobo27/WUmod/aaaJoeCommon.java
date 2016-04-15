package com.Joedobo27.WUmod;

import com.wurmonline.server.items.CreationEntry;
import javassist.*;
import javassist.bytecode.*;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    public static ArrayList largeMaterialRatioDifferentials;

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

    @Override
    public void onServerStarted() {
        if (modifiedCheckSaneAmounts) {
            /*
            try {
                Method setLargeMaterialRatioDifferentials = ReflectionUtil.callPrivateMethod(CreationEntry.class,
                        ReflectionUtil.getMethod(CreationEntry.class, "setLargeMaterialRatioDifferentials"), null);
                setLargeMaterialRatioDifferentials.invoke(null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            */
        }
    }

    @Override
    public void init() {
        pool = HookManager.getInstance().getClassPool();
        logger.log(Level.INFO, "aaaJoeCommon loaded.");
    }

    public static void setLargeMaterialRatioDifferentials() {
        if (largeMaterialRatioDifferentials == null)
            largeMaterialRatioDifferentials = new ArrayList<>(Arrays.asList(73));
    }

    //<editor-fold desc="Javassist and bytecode altering section.">
    private static void setJSSelf() throws Exception{
        ctcSelf = pool.get(com.Joedobo27.WUmod.aaaJoeCommon.class.getName());
    }

    private static void setJSCreationEntry() throws Exception{
        ctcCreationEntry = pool.get("com.wurmonline.server.items.CreationEntry");
        cfCreationEntry = ctcCreationEntry.getClassFile();
        cpCreationEntry = cfCreationEntry.getConstPool();
    }

    private static void setJSForage() {
        try {
            CtClass ctcForage = pool.get("com.wurmonline.server.behaviours.Forage");
            CtClass ctcForageJDB = pool.get("com.Joedobo27.WUmod.ForageJDB");
            CtClass ctcForageDataJDB = pool.get("com.Joedobo27.WUmod.ForageDataJDB");

        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void setJSHerb() {
        try {
            CtClass ctcHerb = pool.get("com.wurmonline.server.behaviours.Herb");
            CtClass ctcHerbJDB = pool.get("com.Joedobo27.WUmod.HerbJDB");
            CtClass ctcHerbDataJDB = pool.get("com.Joedobo27.WUmod.HerbDataJDB");
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void jsForageOverwrite() throws Exception{
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

    public static void jsHerbOverwrite() throws Exception{
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

    public static void jsCheckSaneAmountsExclusions() throws Exception {
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
        */
        //</editor-fold>
        JDBByteCode jbt;
        JDBByteCode jbt1;
        String replaceResult;

        CtMethod cmSetLargeMaterialRatioDifferentials1 = new CtMethod(CtClass.voidType, "setLargeMaterialRatioDifferentials", null, ctcCreationEntry);
        ctcCreationEntry.addMethod(cmSetLargeMaterialRatioDifferentials1);

        logger.log(Level.INFO, ctcSelf.getName());

        CtMethod cmSetLargeMaterialRatioDifferentials2 = ctcSelf.getMethod("setLargeMaterialRatioDifferentials", "()V");
        ClassMap map = new ClassMap();
        map.put("com.Joedobo27.WUmod.aaaJoeCommon", "com.wurmonline.server.items.CreationEntry");
        cmSetLargeMaterialRatioDifferentials1.setBody(cmSetLargeMaterialRatioDifferentials2, map);
        ctcCreationEntry.setModifiers(ctcCreationEntry.getModifiers() & ~Modifier.ABSTRACT);

        FieldInfo f = new FieldInfo(cpCreationEntry, "largeMaterialRatioDifferentials", "Ljava/util/ArrayList;");
        f.setAccessFlags(AccessFlag.PUBLIC);
        f.setAccessFlags(AccessFlag.STATIC);
        cfCreationEntry.addField(f);

        int intCPIndex = cpCreationEntry.addClassInfo("java/lang/Integer");
        int creationEntryCPIndex = Integer.valueOf(JDBByteCode.findConstantPoolReference(cpCreationEntry, ConstPool.CONST_Class, "com/wurmonline/server/items/CreationEntry"), 16);
        int arrayListCPIndex = cpCreationEntry.addClassInfo("java/util/ArrayList");



        setCheckSaneAmounts(cfCreationEntry,
                "(Lcom/wurmonline/server/items/Item;ILcom/wurmonline/server/items/Item;ILcom/wurmonline/server/items/ItemTemplate;Lcom/wurmonline/server/creatures/Creature;Z)V",
                "checkSaneAmounts");
            getCheckSaneAmountsIterator().insertGap(395, 7);
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
                String.format("%04X", cpCreationEntry.addFieldrefInfo(creationEntryCPIndex, "largeMaterialRatioDifferentials", "Ljava/util/ArrayList;") & 0xffff),
                "",
                JDBByteCode.findConstantPoolReference(cpCreationEntry, ConstPool.CONST_Fieldref, "objectCreated:I"),
                String.format("%04X", cpCreationEntry.addMethodrefInfo(intCPIndex, "valueOf", "(I)Ljava/lang/Integer;") & 0xffff),
                String.format("%04X", cpCreationEntry.addMethodrefInfo(arrayListCPIndex, "contains", "(Ljava/lang/Object;)Z") & 0xffff),
                "0025"
                )));
        jbt1.setOpcodeOperand();
        replaceResult = JDBByteCode.byteCodeFindReplace(jbt.getOpcodeOperand(), jbt.getOpcodeOperand(), jbt1.getOpcodeOperand(), getCheckSaneAmountsIterator(),
                "checkSaneAmounts");
        getCheckSaneAmountsMInfo().rebuildStackMapIf6(pool, cfCreationEntry);

        logger.log(Level.INFO, replaceResult);
        JDBByteCode.byteCodePrint(getCheckSaneAmountsIterator(), "checkSaneAmounts",
                "C:\\Program Files (x86)\\Steam\\SteamApps\\common\\Wurm Unlimited Dedicated Server\\byte code prints");
        //modifiedCheckSaneAmounts = true;
    }
    //</editor-fold>

    //<editor-fold desc="Getter and Setter for CodeIterator, CodeAttribute, methodInfo.">
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

    private static CodeIterator getCheckSaneAmountsIterator(){return checkSaneAmountsIterator;}

    private static CodeAttribute getCheckSaneAmountsAttribute(){return checkSaneAmountsAttribute;}

    private static MethodInfo getCheckSaneAmountsMInfo(){return checkSaneAmountsMInfo;}
    //</editor-fold>
}