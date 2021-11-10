package com.company;

import com.company.clone.CloneUtility;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args)  {
        Man man = new Man("Dmitry", 25, new ArrayList<>(Arrays.asList("CLR via C#", "Collector")));
        try{
            Man newM = CloneUtility.cloneTypedValue(man);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}


