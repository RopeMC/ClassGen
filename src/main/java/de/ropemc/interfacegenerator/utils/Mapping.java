package de.ropemc.interfacegenerator.utils;

import lombok.Getter;

import java.io.*;
import java.util.*;

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
                    String[] methodCompleteSignature = split[2].split("\\)");
                    String rawReturnType = methodCompleteSignature[1];
                    String rawParameterTypes = methodCompleteSignature[0].substring(1);
                    List<String> parameterTypes = new ArrayList<>();
                    while(rawParameterTypes.length()>0){
                        char c = rawParameterTypes.charAt(0);
                        rawParameterTypes = rawParameterTypes.substring(1);
                        switch(c){
                            case 'I':
                                parameterTypes.add("int");
                                break;
                            case 'Z':
                                parameterTypes.add("boolean");
                                break;
                            case 'D':
                                parameterTypes.add("double");
                                break;
                            case 'F':
                                parameterTypes.add("float");
                                break;
                            case 'B':
                                parameterTypes.add("byte");
                                break;
                            case 'C':
                                parameterTypes.add("char");
                                break;
                            case 'S':
                                parameterTypes.add("short");
                                break;
                            case 'J':
                                parameterTypes.add("long");
                                break;
                            case 'V':
                                parameterTypes.add("void");
                                break;
                            case 'L':
                                String theType = "";
                                char cc = rawParameterTypes.charAt(0);
                                rawParameterTypes = rawParameterTypes.substring(1);
                                while(cc!=';'){
                                    theType+=cc;
                                    cc = rawParameterTypes.charAt(0);
                                    rawParameterTypes = rawParameterTypes.substring(1);
                                }
                                parameterTypes.add(theType.replace("/","."));
                                break;
                            case '[':
                                int dimensions = 1;
                                while(rawParameterTypes.charAt(0)=='['){
                                    rawParameterTypes=rawParameterTypes.substring(1);
                                    dimensions++;
                                }
                                char ccc = rawParameterTypes.charAt(0);
                                rawParameterTypes = rawParameterTypes.substring(1);
                                String arrayType = "void";
                                switch(ccc){
                                    case 'I':
                                        arrayType="int";
                                        break;
                                    case 'Z':
                                        arrayType="boolean";
                                        break;
                                    case 'D':
                                        arrayType="double";
                                        break;
                                    case 'F':
                                        arrayType="float";
                                        break;
                                    case 'B':
                                        arrayType="byte";
                                        break;
                                    case 'C':
                                        arrayType="char";
                                        break;
                                    case 'S':
                                        arrayType="short";
                                        break;
                                    case 'J':
                                        arrayType="long";
                                        break;
                                    case 'L':
                                        String theType2 = "";
                                        char cccc = rawParameterTypes.charAt(0);
                                        rawParameterTypes = rawParameterTypes.substring(1);
                                        while(cccc!=';'){
                                            theType2+=cccc;
                                            cccc = rawParameterTypes.charAt(0);
                                            rawParameterTypes = rawParameterTypes.substring(1);
                                        }
                                        arrayType=theType2.replace("/",".");
                                        break;
                                }
                                for(int i=0;i<dimensions;i++)
                                    arrayType+="[]";
                                parameterTypes.add(arrayType);
                                System.out.println(orig_clazz);
                                break;
                        }
                    }
                    mm.add(new MethodSignature(orig_method,parseType(rawReturnType),parameterTypes.toArray(new String[0])));
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
        if(rawType.startsWith("L")){
            return rawType.substring(1,rawType.length()-1).replace("/",".");
        }
        switch (rawType){
            case "D":
                return "double";
            case "Z":
                return "boolean";
            case "S":
                return "short";
            case "J":
                return "long";
            case "I":
                return "int";
            case "B":
                return "byte";
            case "C":
                return "char";
            case "F":
                return "float";
        }
        return "void";
    }

}
