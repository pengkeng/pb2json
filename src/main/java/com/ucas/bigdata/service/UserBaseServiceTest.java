package com.example.ucas;

import com.example.ucas.WebApi.InfoRequest;
import com.example.ucas.WebApi.InfoResponse;
import com.google.protobuf.ByteString;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URI;


public class UserBaseServiceTest {
    private static final String DIR = System.getProperty("user.dir");
    private static final String SRC_DIR = DIR + "/src/main/resources";

    public static void main(String[] args) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            URI uri = new URI("http", null, "127.0.0.1", 8082, "/getJson", "", null);
            HttpPost post = new HttpPost(uri);
            InfoRequest.Builder builder = InfoRequest.newBuilder();

            File file1 = new File(SRC_DIR + "/testSchema.proto");
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file1));
                String tempString = null;
                while ((tempString = reader.readLine()) != null) {
                    stringBuilder.append(tempString + "\n");
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            BufferedInputStream bis = null;
            bis = new BufferedInputStream(new FileInputStream(SRC_DIR + "/testData"));
            int len = bis.available();
            byte[] b = new byte[len];
            bis.read(b, 0, len);

            builder.setSchema(stringBuilder.toString());
            builder.setData(ByteString.copyFrom(b));
            post.setEntity(new ByteArrayEntity(builder.build().toByteArray()));
            post.setHeader("Content-Type", "application/x-protobuf");

            HttpResponse response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {

                InfoResponse resp = InfoResponse.parseFrom(response.getEntity().getContent());

                System.out.println("result:" + resp.getData() + " " + resp.getMsg() + " " + resp.getCode());
            } else {
                System.out.println(response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            httpClient.close();
        }
    }
}
