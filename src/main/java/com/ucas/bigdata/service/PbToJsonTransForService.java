package com.ucas.bigdata.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.ucas.bigdata.proto.WebApi;
import org.springframework.stereotype.Service;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Service
public class PbToJsonTransForService {

    public final String DIR = "/Users/pqc/Desktop/class";
    private final String SRC_DIR = DIR;
    private final String DST_DIR = DIR;
    private final String PACKAGE_DIR = DST_DIR;
    private String className;
    public static String CLASS_COMPILER_FAIL = "Compiler fail";
    public static String GENERATE_JAVA_FAIL = "generate Java fail";
    public static String UNKNOW_FAIL = "server fail";


    public WebApi.InfoResponse.Builder getInfo(String schema, ByteString data, String version) {
        WebApi.InfoResponse.Builder builder = WebApi.InfoResponse.newBuilder();
        try {
            String json = getJson(schema, data, version);
            if (CLASS_COMPILER_FAIL.equals(json)) {
                builder.setCode(500);
                builder.setMsg(CLASS_COMPILER_FAIL);
            } else if (GENERATE_JAVA_FAIL.equals(json)) {
                builder.setCode(501);
                builder.setMsg(GENERATE_JAVA_FAIL);
            } else if (UNKNOW_FAIL.equals(json)) {
                builder.setMsg(UNKNOW_FAIL);
                builder.setCode(502);
            } else {
                builder.setCode(200);
                builder.setMsg("success");
                builder.setData(json);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder;
    }

    public String getJson(String schema, ByteString data, String version) throws IOException {
        File schemaFile = null;
        schemaFile = createSchemaFile(schema, version);
        generateJavaFile(schemaFile);
        try {
            File javaFile = new File(PACKAGE_DIR + "/" + className + ".java");
            if (javaFile.exists()) {
                JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
                int status = javac.run(null, null, null, "-classpath", DIR + "/" + "protobuf-java-3.10.0.jar", DIR + "/" + className + ".java");
                if (status == 0) {
                    Class<?> schemaClass = null;

                    schemaClass =  Class.forName( className, true, new MyClassLoader()).getClasses()[0];

//                    URL[] urls = {new URL("file:" + DIR + "/")};
//                    URLClassLoader urlPathClassLoader = new URLClassLoader(urls, getSystemClassLoader());
//                    schemaClass = urlPathClassLoader.loadClass(className).getClasses()[0];
                    System.out.println(schemaClass);
                    Method[] a = schemaClass.getMethods();
                    Method method = null;
                    byte[] byteData = data.toByteArray();
                    for (int i = 0; i < a.length; i++) {
                        System.out.println(a[i].getName() + " ");
                        if (a[i].getParameterTypes().length == 1) {
                            System.out.println((a[i].getParameterTypes()[0]));
                        }
                        if ("parseFrom".equals(a[i].getName()) && a[i].getParameterTypes().length == 1 && "[B".equals(a[i].getParameterTypes()[0].getName())) {
                            method = a[i];
                        }
                    }
                    System.out.println(method.getName() + " ");

                    Object object =  method.invoke(schemaClass, (Object) byteData);
                    String json = new JsonFormat().printToString((Message) object);
                    File classFile = new File(DIR + "/" + className + ".class");
                    javaFile.delete();
                    classFile.delete();
                    schemaFile.delete();
                    return json;
                } else {
                    return CLASS_COMPILER_FAIL;
                }
            } else {
                return GENERATE_JAVA_FAIL;
            }
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return UNKNOW_FAIL;
    }

    private void generateJavaFile(File schemaFile) throws IOException {
        if (schemaFile.exists()) {
            String shell = "protoc3 -I=" + SRC_DIR + " --java_out=" + DST_DIR + " " + SRC_DIR + "/PBSchema.proto";
            Runtime.getRuntime().exec(shell);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private File createSchemaFile(String schema, String version) throws IOException {
        System.out.println(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        System.out.println(DIR);
        File file = new File(SRC_DIR + "/PBSchema.proto");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        FileWriter writer = new FileWriter(file, true);
        long time = System.nanoTime();
        className = "PBSchema" + time;
        String pac = "option java_package = \"com.ucas.bigdata.proto\";\n";
        if ("2".equals(version)) {
            writer.append("syntax = \"proto2\";\n" + "option java_outer_classname = \"" + className + "\";\n");
        } else if ("3".equals(version)) {
            writer.append("syntax = \"proto3\";\n" + "option java_outer_classname = \"" + className + "\";\n");
        }
        writer.append(schema);
        writer.flush();
        return file;
    }
}
