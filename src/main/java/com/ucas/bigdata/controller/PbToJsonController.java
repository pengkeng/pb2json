package com.ucas.bigdata.controller;

import com.ucas.bigdata.proto.WebApi;
import com.ucas.bigdata.service.PbToJsonTransForService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PbToJsonController {
    private static Logger logger = LoggerFactory.getLogger(com.ucas.bigdata.controller.PbToJsonController.class);

    @Autowired
    private PbToJsonTransForService transforService;

    @RequestMapping(value = "/getJson", method = RequestMethod.POST, produces = "application/x-protobuf")
    public @ResponseBody
    WebApi.InfoResponse getUserInfo(@RequestBody WebApi.InfoRequest request) throws Exception {
        logger.info("请求：{}", request.toString());
        String jsonData = transforService.getInfo(request.getSchema(), request.getData());

        WebApi.InfoResponse.Builder builder = WebApi.InfoResponse.newBuilder();
        if (jsonData != null) {
            builder.setCode(200);
            builder.setMsg("success");
            builder.setData(jsonData);
        }
        return builder.build();
    }
}
