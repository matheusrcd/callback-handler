package br.com.bradesco.bpw.callbackhandler.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping(value = "/.well-known/pki-validation/")
@Slf4j
public class ValidateCertificateController {
    @GetMapping(path = "/BD457CB07E68EB0634B1FF8C7FC208FD.txt")
    public ResponseEntity<Resource> download(String param) throws IOException {
        File certFile = new File("certs/BD457CB07E68EB0634B1FF8C7FC208FD.txt");
        InputStreamResource resource = new InputStreamResource(new FileInputStream(certFile));

        return ResponseEntity.ok()
                .contentLength(certFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
