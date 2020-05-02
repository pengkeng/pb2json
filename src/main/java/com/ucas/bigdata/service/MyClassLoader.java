package com.ucas.bigdata.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyClassLoader extends ClassLoader {
    public final String DIR = "/Users/pqc/Desktop/class";

    @Override
    protected Class<?> findClass(String className)
            throws ClassNotFoundException {
        if(!className.contains("PBSchema")){
            super.findClass(className);
        }
        byte[] cLassBytes = null;
        try {
            Path path = Paths.get(new URI("file:" + DIR + "/" + className + ".class"));
            cLassBytes = Files.readAllBytes(path);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        Class cLass = defineClass(cLassBytes, 0, cLassBytes.length);
        return cLass;
    }
}
