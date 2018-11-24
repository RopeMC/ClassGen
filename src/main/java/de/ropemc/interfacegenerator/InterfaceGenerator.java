package de.ropemc.interfacegenerator;

import de.ropemc.interfacegenerator.utils.Mapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;

public class InterfaceGenerator {

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
        String outputClassName = InterfaceBuilder.getRootPackage()+"."+className;
        File outputFile = new File(folder,outputClassName.replace(".","/")+".java");
        writeFile(outputFile,new InterfaceBuilder(mapping,className).build());
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
}
