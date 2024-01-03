package br.com.bradesco.bpw.callbackhandler.controller;

import br.com.bradesco.bpw.callbackhandler.service.CallbackHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class CallbackHandlerController {

    @Autowired
    private CallbackHandlerService callbackHandlerService;

    @GetMapping(value = "/")
    @ResponseBody
    public String retornarCallback() {
        return "Hello world!";
    }
}
