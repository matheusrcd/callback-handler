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
    public ResponseEntity<String> retornarCallback(@RequestBody String encryptedBody) {
        try {
            PayloadTransaction payloadTransaction = callbackHandlerService.processRequest(encryptedBody);
            return ResponseEntity.ok(callbackHandlerService.processTransaction(payloadTransaction));
        } catch (Exception e) {
            logger.error(e.toString());
            return ResponseEntity.badRequest().body("");
        }
    }

    @GetMapping(value = "/getTx")
    @ResponseBody
    public ResponseEntity<String> getTx() {
        return ResponseEntity.ok("Sucesso");
    }
}
