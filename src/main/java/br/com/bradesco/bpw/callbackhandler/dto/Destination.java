package br.com.bradesco.bpw.callbackhandler.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Destination {
    private double amountNative;
    private String amountNativeStr;
    private double amountUSD;
    private String dstAddressType;
    private String dstId;
    private String dstWalletId;
    private String dstName;
    private String dstSubType;
    private String dstType;
    private String displayDstAddress;
    private String action;
    private ActionInfo actionInfo;

}
