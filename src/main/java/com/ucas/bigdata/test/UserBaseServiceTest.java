package com.ucas.bigdata.test;

import com.ucas.bigdata.proto.WebApi.InfoRequest;
import com.ucas.bigdata.proto.WebApi.InfoResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;


public class UserBaseServiceTest {
    public static void main(String [] args) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            URI uri = new URI("http", null, "localhost.charlesproxy.com", 8080, "/getJson", "", null);
            HttpPost post = new HttpPost(uri);
            InfoRequest.Builder builder = InfoRequest.newBuilder();
            builder.setData("me");
            builder.setSchema("123");
            post.setEntity(new ByteArrayEntity(builder.build().toByteArray()));
            post.setHeader("Content-Type", "application/x-protobuf");

            HttpResponse response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() == 200) {

                InfoResponse resp = InfoResponse.parseFrom(response.getEntity().getContent());

                System.out.println("result:" + resp.getData() + " " + resp.getMsg() + " " +resp.getCode());
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
