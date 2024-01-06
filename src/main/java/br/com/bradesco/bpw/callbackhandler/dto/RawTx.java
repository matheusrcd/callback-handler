package br.com.bradesco.bpw.callbackhandler.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RawTx {
    private String keyDerivationPath;
    private String rawTx;
    private String payload;

}