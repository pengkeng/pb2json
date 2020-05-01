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

    private final String DIR = System.getProperty("user.dir");
    private final String SRC_DIR = DIR + "/src/main/resources";
    private final String DST_DIR = DIR + "/src/main/java";
    private final String PACKAGE_DIR = DST_DIR + "/com/ucas/bigdata/proto";
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
            } else{
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
                int status = javac.run(null, null, null, "-d", System.getProperty("user.dir") + "/target/classes", PACKAGE_DIR + "/" + className + ".java");
                if (status == 0) {
                    Class<?> schemaClass = null;
                    schemaClass = ClassLoader.getSystemClassLoader().loadClass("com.ucas.bigdata.proto." + className).getClasses()[0];
                    Method method = schemaClass.getDeclaredMethod("parseFrom", ByteString.class);
                    method.setAccessible(true);
                    Message message = (Message) method.invoke(schemaClass, data);
                    String json = new JsonFormat().printToString(message);
                    File classFile = new File(System.getProperty("user.dir") + "/target/classes/com/ucas/bigdata/proto/" + className + ".class");
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
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
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
        File file = new File(SRC_DIR + "/PBSchema.proto");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        FileWriter writer = new FileWriter(file, true);
        long time = System.nanoTime();
        className = "PBSchema" + time;
        if ("2".equals(version)) {
            writer.append("syntax = \"proto2\";\n" + "option java_package = \"com.ucas.bigdata.proto\";\n" + "option java_outer_classname = \"" + className + "\";\n");
        } else if ("3".equals(version)) {
            writer.append("syntax = \"proto3\";\n" + "option java_package = \"com.ucas.bigdata.proto\";\n" + "option java_outer_classname = \"" + className + "\";\n");
        }
        writer.append(schema);
        writer.flush();
        return file;
    }
}
