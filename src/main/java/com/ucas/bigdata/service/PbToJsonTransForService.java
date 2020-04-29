package com.ucas.bigdata.service;

import org.springframework.stereotype.Service;

@Service
public class PbToJsonTransForService {
    public String getInfo(String schema,String data){
        //todo 这里实现
        System.out.println(schema+"----"+data);
        return "hello";
    }
}
