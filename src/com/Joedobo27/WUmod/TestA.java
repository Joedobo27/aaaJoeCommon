package com.Joedobo27.WUmod;

import java.util.ArrayList;
import java.util.Arrays;

public class TestA {

    public static ArrayList<Integer> largeMaterialRatioDifferentials;
    int objectCreated;

    TestA(int a){
        this.objectCreated = a;
    }

    public void abc(){
        if (!largeMaterialRatioDifferentials.contains(this.objectCreated)){
            // bla bla
        }
    }

    public static ArrayList setLargeMaterialRatioDifferentials() {
        return new ArrayList<>(Arrays.asList(73));
    }
}