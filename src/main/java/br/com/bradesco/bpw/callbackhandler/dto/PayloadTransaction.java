package br.com.bradesco.bpw.callbackhandler.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayloadTransaction {
    private String txId;
    private String operation;
    private String sourceType;
    private String sourceId;
    private String destType;
    private String destId;
    private String asset;
    private double amount;
    private String amountStr;
    private double requestedAmount;
    private String requestedAmountStr;
    private String fee;
    private String destAddressType;
    private String destAddress;
    private List<Destination> destinations;
    private List<RawTx> rawTx;
    private List<String> players;
    private String requestId;
}
