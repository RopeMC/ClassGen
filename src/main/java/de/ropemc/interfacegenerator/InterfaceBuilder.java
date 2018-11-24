package de.ropemc.interfacegenerator;

import de.ropemc.interfacegenerator.utils.Mapping;
import de.ropemc.interfacegenerator.utils.MethodSignature;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class InterfaceBuilder {

    @Getter
    private static String rootPackage = "de.ropemc.api.wrapper";

    private static String[] autoImported = {
            "java.lang.String",
            "java.lang.Class",
            "java.lang.Integer",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Short",
            "java.lang.Long"
    };

    private Mapping mapping;
    private String className;

    private List<String> imports = new ArrayList<>();
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
            for(String is : imports)
                sb.append("import "+is+";\n");
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
                prototypes.add(returnType +" "+ms.getName()+"("+params+");");
            }
        }
        addImport("de.ropemc.api.wrapper.WrappedClass");
    }

    private void addImport(String className){
        if(!imports.contains(className))
            imports.add(className);
    }

    private String preprocessClass(String className){
        String pClassName = prePreprocessClass(className);
        if(pClassName.contains(".")){
            String iClassName = pClassName.replace("[]","");
            addImport(iClassName);
            String[] classSplit = pClassName.split("\\.");
            return classSplit[classSplit.length-1];
        }
        return pClassName;
    }

    private String prePreprocessClass(String className){
        String processClassName = className;
        for(String s : autoImported){
            String[] sSplit = s.split("\\.");
            processClassName = processClassName.replace(s,sSplit[sSplit.length-1]);
        }
        if(processClassName.startsWith("net.minecraft."))
            return rootPackage+"."+processClassName;
        return processClassName;
    }

}
