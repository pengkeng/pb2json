package com.ucas.bigdata.controller;

import com.ucas.bigdata.proto.WebApi;
import com.ucas.bigdata.service.PbToJsonTransForService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PbToJsonController {
    private static Logger logger = LoggerFactory.getLogger(com.ucas.bigdata.controller.PbToJsonController.class);

    @Autowired
    private PbToJsonTransForService transforService;

    @RequestMapping(value = "/getJson", method = RequestMethod.POST, produces = "application/x-protobuf")
    public @ResponseBody
    WebApi.InfoResponse getUserInfo(@RequestBody WebApi.InfoRequest request, @RequestParam(value = "version", defaultValue = "3") String version) throws Exception {
        WebApi.InfoResponse.Builder builder = transforService.getInfo(request.getSchema(), request.getData(), version);
        return builder.build();
    }
}
