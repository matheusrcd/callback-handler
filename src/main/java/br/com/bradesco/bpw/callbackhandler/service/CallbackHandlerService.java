package br.com.bradesco.bpw.callbackhandler.service;

import br.com.bradesco.bpw.callbackhandler.controller.CallbackHandlerController;
import br.com.bradesco.bpw.callbackhandler.dto.CallbackResponse;
import br.com.bradesco.bpw.callbackhandler.dto.PayloadTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

@Service
public class CallbackHandlerService {
    Logger logger = LoggerFactory.getLogger(CallbackHandlerService.class);

    @Value("${privateKeyPath}")
    private String privateKeyPath;

    @Value("${cosignerPublicKeyPath}")
    private String cosignerPublicKeyPath;
    private SecretKeySpec privateKey;
    private SecretKeySpec cosignerPubKey;

    @Autowired
    private ObjectMapper objectMapper;

    public PayloadTransaction processTxRequest(String encryptedBody) {
        if (!validateJwt(encryptedBody)){ return null; }
        String txDecrypted = parseTxJwt(encryptedBody);

        try{
            PayloadTransaction payloadTransaction = objectMapper.readValue(txDecrypted, PayloadTransaction.class);
            logger.info("Transação recebida e validada com sucesso: " + payloadTransaction.getRequestId());
            return payloadTransaction;
        }catch (JsonProcessingException e){
            logger.error("ERRO AO VALIDAR TRANSACAO: " + e.getStackTrace());
        }
        return null;
    }

    public String processTransaction(PayloadTransaction payloadTransaction) {
        if (validateTransaction(payloadTransaction)) {
            logger.info("Aprovando transação: " + payloadTransaction.getRequestId());
            return generateSignedResponse("APPROVE", payloadTransaction.getRequestId(), "");
        } else {
            logger.info("Rejeitando transação");
            if (payloadTransaction == null){return "";}
            return generateSignedResponse("REJECT", payloadTransaction.getRequestId(), "Failed to validate ETH transaction");
        }
    }

    private boolean validateTransaction(PayloadTransaction payload) {
        if (payload == null){return false;}
        return true;
    }

    private String generateSignedResponse(String action, String requestId, String rejectionReason) {
        CallbackResponse callbackResponse = new CallbackResponse(action,requestId, rejectionReason);

        if (rejectionReason.equals("")){
            callbackResponse = new CallbackResponse(action,requestId);
        }

        try{
            logger.info("Gerando resposta: " + callbackResponse);
            String callbackResponseJson = objectMapper.writeValueAsString(callbackResponse);
            String jwtResponse = Jwts.builder()
                    .setSubject(callbackResponseJson)
                    .signWith(privateKey)
                    .compact();
            return jwtResponse;
        }catch (JsonProcessingException e){
            logger.error("ERRO AO CRIAR RESPOSTA: " + e.getStackTrace());
        }
        return "";
    }

    private boolean validateJwt(String encryptedBody) {
        readSigningKeys();
        try{
            logger.info("Validando requisição com chave pública");
            Jwts.parserBuilder()
                    .setSigningKey(cosignerPubKey)
                    .build()
                    .parse(encryptedBody);
            return true;
        } catch (Exception e){
            logger.error("ERRO AO VALIDAR JWT: " + e.getStackTrace());
        }
        return false;
    }
    private void readSigningKeys(){
        logger.info("Lendo chave privada e chave pública");
        try{
            //privateKey = new String(Files.readAllBytes(Paths.get(privateKeyPath)));
            //cosignerPubKey = new String(Files.readAllBytes(Paths.get(cosignerPublicKeyPath)));
            SignatureAlgorithm sa = SignatureAlgorithm.HS256;
            cosignerPubKey = new SecretKeySpec(cosignerPublicKeyPath.getBytes(), sa.getJcaName());
            privateKey = new SecretKeySpec(privateKeyPath.getBytes(), sa.getJcaName());

            if (cosignerPublicKeyPath.isBlank() || privateKeyPath.isEmpty()){throw new IOException();} // TESTE
        } catch (IOException e){
            logger.error("ERRO AO ABRIR CHAVES: " + e.getStackTrace());
        }
    }

    private String parseTxJwt(String encryptedBody) {
        logger.info("Decodificando valor recebido");
        String[] split_string = encryptedBody.split("\\.");
        String base64EncodedBody = split_string[1];
        String txDecrypted = new String(Base64.getDecoder().decode(base64EncodedBody));
        return txDecrypted;
    }

}
