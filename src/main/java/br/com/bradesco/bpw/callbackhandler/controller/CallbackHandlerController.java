package br.com.bradesco.bpw.callbackhandler.controller;

import br.com.bradesco.bpw.callbackhandler.dto.CallbackResponse;
import br.com.bradesco.bpw.callbackhandler.dto.PayloadTransaction;
import br.com.bradesco.bpw.callbackhandler.service.CallbackHandlerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/v2")
@Slf4j
public class CallbackHandlerController {
    Logger logger = LoggerFactory.getLogger(CallbackHandlerController.class);

    @Autowired
    private CallbackHandlerService callbackHandlerService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/tx_sign_request", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> transactionSigning(@RequestBody String encryptedBody) {
        PayloadTransaction payloadTransaction = callbackHandlerService.processTxRequest(encryptedBody);
        String callbackResponse = callbackHandlerService.processTransaction(payloadTransaction);
        if (!callbackResponse.equals("")) {
            return ResponseEntity.ok(callbackResponse);
        }
        return ResponseEntity.badRequest().body("");
    }

    @PostMapping(value = "/config_change_sign_request", consumes = "application/jwt")
    @ResponseBody
    public ResponseEntity<String> configurationApprovalCallback(@RequestBody String encryptedBody) {
        PayloadTransaction payloadTransaction = callbackHandlerService.processTxRequest(encryptedBody);
        String callbackResponse = callbackHandlerService.processTransaction(payloadTransaction);
        if (!callbackResponse.equals("")) {
            return ResponseEntity.ok(callbackResponse);
        }
        return ResponseEntity.badRequest().body("");
    }

    @GetMapping(value = "/getTx")
    @ResponseBody
    public ResponseEntity<String> getTx() {
        return ResponseEntity.ok("Sucesso");
    }
}
