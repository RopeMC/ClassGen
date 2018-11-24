package de.ropemc.interfacegenerator;

import de.ropemc.interfacegenerator.utils.Mapping;
import de.ropemc.interfacegenerator.utils.MethodSignature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class InterfaceGenerator {

    private static String rootPackage = "de.ropemc.api.wrapper";

    public static void main(String [] args){
        Mapping mapping = new Mapping(new File("mappings.srg"));
        File outputFolder = new File("output");
        if(outputFolder.exists())
            deleteFolder(outputFolder);
        outputFolder.mkdir();
        for(String s : mapping.getClasses())
            if(!s.contains("$"))
                new Thread(() -> {
                    generateClass(mapping,s,outputFolder);
                }).start();
    }

    private static void generateClass(Mapping mapping,String className,File folder){
        String[] classNameSplit = className.split("\\.");
        String classNameSimple = classNameSplit[classNameSplit.length-1];
        String classPackage = "";
        for(int i=0;i<classNameSplit.length-1;i++)
            classPackage+="."+classNameSplit[i];
        classPackage = classPackage.substring(1);
        String outputClassName = rootPackage+"."+className;
        File outputFile = new File(folder,outputClassName.replace(".","/")+".java");
        StringBuilder sb = new StringBuilder();
        sb.append("package "+rootPackage+"."+classPackage+";\n\n");
        sb.append("@WrappedClass(\"" + className + "\")\n");
        sb.append("public interface "+classNameSimple+" {\n");
        if(mapping.getMethods().containsKey(className)){
            for(MethodSignature ms : mapping.getMethods().get(className)){
                String params = "";
                int i=0;
                for(String pt : ms.getParameterTypes()){
                    if(params.length()>0)
                        params+=", ";
                    params+=pt+" var"+i;
                    i++;
                }
                String thePackage = processPackage(ms.getReturnType());
                sb.append("    "+ thePackage +" "+ms.getName()+"("+params+");\n\n");
            }
        }else{
            sb.append("\n");
        }
        sb.append("}\n");
        writeFile(outputFile,sb.toString());
    }

    private static void writeFile(File file,String content){
        try {
            createFolders(file.getParentFile());
            if(!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.flush();
            fos.close();
        }catch (Exception ex){ex.printStackTrace();}
    }

    private static void createFolders(File file){
        if(file.getParentFile()!=null)
            createFolders(file.getParentFile());
        if(!file.exists())
            file.mkdir();
    }

    private static void deleteFolder(File file){
        try {
            deleteFolder(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteFolder(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteFolder(entry);
                }
            }
        }
        Files.delete(path);
    }

    private static String processPackage(String classWithPackage) {
        if(classWithPackage.endsWith(".String")) {
            classWithPackage = "String";
        }
        return classWithPackage;
    }
}
