package de.ropemc.interfacegenerator.utils;

import lombok.Getter;

@Getter
public class MethodSignature {
    private String name;
    private String returnType;
    private String[] parameterTypes;
    public MethodSignature(String name,String returnType,String... parameterTypes){
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }
}
