package br.com.bradesco.bpw.callbackhandler.service;

import br.com.bradesco.bpw.callbackhandler.controller.CallbackHandlerController;
import br.com.bradesco.bpw.callbackhandler.dto.CallbackResponse;
import br.com.bradesco.bpw.callbackhandler.dto.PayloadTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Service
public class CallbackHandlerService {
    Logger logger = LoggerFactory.getLogger(CallbackHandlerService.class);

    @Value("${privateKeyPath}")
    private String privateKeyPath;

    @Value("${cosignerPublicKeyPath}")
    private String cosignerPublicKeyPath;
    private PrivateKey privateKey;
    private PublicKey cosignerPubKey;

    @Autowired
    private ObjectMapper objectMapper;

    public PayloadTransaction processTxRequest(String encryptedBody) {
        if (!validateJwt(encryptedBody)) {
            return null;
        }
        String txDecrypted = parseTxJwt(encryptedBody);

        try {
            PayloadTransaction payloadTransaction = objectMapper.readValue(txDecrypted, PayloadTransaction.class);
            logger.info("Transação recebida e validada com sucesso: " + payloadTransaction.getRequestId());
            return payloadTransaction;
        } catch (JsonProcessingException e) {
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
            if (payloadTransaction == null) {
                return "";
            }
            return generateSignedResponse("REJECT", payloadTransaction.getRequestId(), "Failed to validate ETH transaction");
        }
    }

    private boolean validateTransaction(PayloadTransaction payload) {
        if (payload == null) {
            return false;
        }
        return true;
    }

    private String generateSignedResponse(String action, String requestId, String rejectionReason) {
        CallbackResponse callbackResponse = new CallbackResponse(action, requestId, rejectionReason);
        if (rejectionReason.equals("")) {
            callbackResponse = new CallbackResponse(action, requestId);
        }
        logger.info("Gerando resposta: " + callbackResponse);
        String jwtResponse = Jwts.builder()
                .setClaims(callbackResponse.getCallbackResponseAsClaims())
                .signWith(privateKey)
                .compact();
        return jwtResponse;
    }

    private boolean validateJwt(String encryptedBody) {
        readSigningKeys();
        try {
            logger.info("Validando requisição com chave pública");
            Jwts.parserBuilder()
                    .setSigningKey(cosignerPubKey)
                    .build()
                    .parse(encryptedBody);
            return true;
        } catch (Exception e) {
            logger.error("ERRO AO VALIDAR JWT: " + e.getStackTrace());
        }
        return false;
    }

    private void readSigningKeys() {
        logger.info("Lendo chave privada e chave pública");
        try {
            this.privateKey = extractPrivateKey(privateKeyPath);
            this.cosignerPubKey = extractPublicKey(cosignerPublicKeyPath);

            if (cosignerPublicKeyPath.isBlank() || privateKeyPath.isEmpty()) {
                throw new IOException();
            }
        } catch (IOException e) {
            logger.error("ERRO AO ABRIR ARQUIVOS DE CHAVES: " + e.getStackTrace());
        } catch (Exception e) {
            logger.error("ERRO AO ABRIR CHAVES: " + e.getStackTrace());
        }
    }

    private static PrivateKey extractPrivateKey(String filePath) throws Exception {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PEMKeyPair keyPair = (PEMKeyPair) pemParser.readObject();
            return converter.getPrivateKey(keyPair.getPrivateKeyInfo());
        }
    }

    private static PublicKey extractPublicKey(String filePath) throws Exception {
        try (PEMParser pemParser = new PEMParser(new FileReader(filePath))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) pemParser.readObject();
            return converter.getPublicKey(publicKeyInfo);
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
