package de.ropemc.interfacegenerator;

import de.ropemc.interfacegenerator.utils.Mapping;
import de.ropemc.interfacegenerator.utils.MethodSignature;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class InterfaceBuilder {

    @Getter
    private static String rootPackage = "de.ropemc.api.wrapper";

    private static String[] commentList = {
            "$",
            "tv.twitch",
            "com.mojang",
            "com.google.common"
    };

    private Mapping mapping;
    private String className;

    private List<String> imports = new ArrayList<>();
    private List<String> importedCommentList = new ArrayList<>();
    private List<String> prototypes = new ArrayList<>();

    public InterfaceBuilder(Mapping mapping, String className){
        this.mapping = mapping;
        this.className = className;
    }

    public String build(){
        process();
        String[] classNameSplit = className.split("\\.");
        String classNameSimple = classNameSplit[classNameSplit.length-1];
        String classPackage = "";
        for(int i=0;i<classNameSplit.length-1;i++)
            classPackage+="."+classNameSplit[i];
        classPackage = classPackage.substring(1);
        StringBuilder sb = new StringBuilder();
        sb.append("package "+rootPackage+"."+classPackage+";\n\n");
        if(imports.size()>0){
            for(String is : imports){
                if(is.startsWith("//")){
                    sb.append("//import "+is.substring(2)+";\n");
                }else{
                    sb.append("import "+is+";\n");
                }
            }
            sb.append("\n");
        }
        sb.append("@WrappedClass(\"" + className + "\")\n");
        sb.append("public interface "+classNameSimple+" {\n\n");
        if(prototypes.size()>0){
            for(String proto : prototypes){
                sb.append("    "+proto+"\n\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    private void process(){
        imports.clear();
        prototypes.clear();
        if(mapping.getMethods().containsKey(className)){
            for(MethodSignature ms : mapping.getMethods().get(className)){
                String params = "";
                int i=0;
                for(String pt : ms.getParameterTypes()){
                    if(params.length()>0)
                        params+=", ";
                    params+=preprocessClass(pt)+" var"+i;
                    i++;
                }
                String returnType = preprocessClass(ms.getReturnType());
                String proto = returnType +" "+ms.getName()+"("+params+");";
                for(String s : commentList){
                    if(proto.contains(s)){
                        proto = "//"+proto;
                        break;
                    }
                }
                if(!proto.startsWith("//")){
                    for(String s : importedCommentList){
                        if(proto.contains(s)){
                            proto = "//"+proto;
                            break;
                        }
                    }
                }
                prototypes.add(proto);
            }
        }
        List<String> newImports = new ArrayList<>();
        for(String i : imports){
            for(String s : commentList){
                if(i.contains(s)){
                    i = "//"+i;
                    break;
                }
            }
            newImports.add(i);
        }
        imports = newImports;
    }

    private void addImport(String className){
        String classPackage = extractPackage(prePreprocessClass(this.className));
        String importPackage = extractPackage(className);
        if(importPackage.equals(classPackage))
            return;
        if(!imports.contains(className))
            imports.add(className);
    }

    private String extractPackage(String className){
        String[] split = className.split("\\.");
        return className.substring(0,className.length()-split[split.length-1].length());
    }

    private String preprocessClass(String className){
        String pClassName = prePreprocessClass(className);
        if(pClassName.contains(".")){
            String iClassName = pClassName.replace("[]","");
            addImport(iClassName);
            String[] classSplit = pClassName.split("\\.");
            String shortClassName = classSplit[classSplit.length-1];
            for(String s : commentList)
                if(pClassName.contains(s))
                    importedCommentList.add(shortClassName);
            return shortClassName;
        }
        return pClassName;
    }

    private String prePreprocessClass(String className){
        String processClassName = className;
        if(processClassName.startsWith("java.lang.")){
            String[] sSplit = processClassName.split("\\.");
            processClassName = sSplit[sSplit.length-1];
        }
        if(processClassName.startsWith("net.minecraft."))
            return rootPackage+"."+processClassName;
        return processClassName;
    }

}
