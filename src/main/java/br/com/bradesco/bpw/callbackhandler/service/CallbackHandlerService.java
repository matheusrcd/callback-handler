package br.com.bradesco.bpw.callbackhandler.service;

import br.com.bradesco.bpw.callbackhandler.controller.CallbackHandlerController;
import br.com.bradesco.bpw.callbackhandler.dto.CallbackResponse;
import br.com.bradesco.bpw.callbackhandler.dto.PayloadTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CallbackHandlerService {
    Logger logger = LoggerFactory.getLogger(CallbackHandlerService.class);

    private String secretKey = "";
    @Autowired
    private ObjectMapper objectMapper;
    public String processTransaction(PayloadTransaction payloadTransaction) throws JsonProcessingException {

        String callbackResponse;
        if (validateTransaction(payloadTransaction)) {
            callbackResponse = generateSignedResponse("APPROVE", payloadTransaction.getRequestId(), "");
        } else {
            callbackResponse = generateSignedResponse("REJECT", payloadTransaction.getRequestId(), "Failed to validate ETH transaction");
        }

        return callbackResponse;
    }

    private boolean validateTransaction(PayloadTransaction payload) {
        return true;
    }
    public PayloadTransaction processRequest(String encryptedBody) throws JsonProcessingException {
        logger.info("RECEBIDO DA FIREBLOCKS: " + encryptedBody);
        String decryptedBody = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(encryptedBody)
                .getBody()
                .toString();
        PayloadTransaction payloadTransaction = objectMapper.readValue(decryptedBody, PayloadTransaction.class);
        logger.info("ENVIO FIREBLOCKS PROCESSADO: " + payloadTransaction);
        return payloadTransaction;
    }

    private String generateSignedResponse(String action, String requestId, String rejectionReason) throws JsonProcessingException {
        CallbackResponse callbackResponse = new CallbackResponse(action,requestId, rejectionReason);
        if (rejectionReason.equals("")){
            callbackResponse = new CallbackResponse(action,requestId);
        }

        String jsonResponse = objectMapper.writeValueAsString(callbackResponse);

        String jwtResponse = Jwts.builder()
                .setSubject(jsonResponse)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return jwtResponse;
    }
}
