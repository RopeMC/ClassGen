package de.ropemc.interfacegenerator.utils;

import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapping
{

    @Getter
    private List<String> classes = new ArrayList<>();
    @Getter
    private Map<String, List<MethodSignature>> methods = new HashMap<>();

    public Mapping(File file)
    {
        this(getStream(file));
    }

    private static InputStream getStream(File file){
        try {
            return new FileInputStream(file);
        } catch (Exception ex) { }
        return null;
    }

    public Mapping(InputStream stream){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = br.readLine()) != null){
                if (line.startsWith("CL:")){
                    String[] split = line.split(" ");
                    classes.add(split[1].replace('/', '.'));
                }
                else if (line.startsWith("MD:"))
                {
                    String[] split = line.split(" ");
                    String[] orig_split = split[1].split("/");
                    String orig_clazz = "";
                    for (int i = 0; i < orig_split.length - 1; i++)
                    {
                        if (orig_clazz.length() > 0) orig_clazz += ".";
                        orig_clazz += orig_split[i];
                    }
                    String orig_method = orig_split[orig_split.length - 1];
                    List<MethodSignature> mm = null;
                    if (methods.containsKey(orig_clazz))
                    {
                        mm = methods.get(orig_clazz);
                    }
                    else
                    {
                        mm = new ArrayList<>();
                    }
                    String methodCompleteSignature = split[2];
                    String rawReturnType = methodCompleteSignature.split("\\)")[1];
                    mm.add(new MethodSignature(orig_method,parseType(rawReturnType)));
                    methods.put(orig_clazz, mm);
                }
            }
            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String parseType(String rawType){
        if(rawType.endsWith(";")){
            return rawType.substring(1,rawType.length()-1).replace("/",".");
        }
        switch (rawType){
            case "D":
                return "double";
            case "Z":
                return "boolean";
            case "I":
                return "int";
        }
        return "void";
    }

}
