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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

@Service
public class CallbackHandlerService {
    Logger logger = LoggerFactory.getLogger(CallbackHandlerService.class);

    private String secretKeyString = "abc";
    KeyPair keyPair;
    PrivateKey privateKey;
    PublicKey publicKey;
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
    public PayloadTransaction processRequest(String encryptedBody) throws Exception {
        keyPair = generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        String decryptedBody = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(encryptedBody)
                .getBody()
                .toString();
        PayloadTransaction payloadTransaction = objectMapper.readValue(decryptedBody, PayloadTransaction.class);
        logger.info("ENVIO FEITO PELA FIREBLOCKS: " + payloadTransaction);
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
                .signWith(SignatureAlgorithm.HS256, privateKey)
                .compact();
        return jwtResponse;
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("HS256");
        keyPairGenerator.initialize(2048); // Tamanho da chave
        return keyPairGenerator.generateKeyPair();
    }
}
