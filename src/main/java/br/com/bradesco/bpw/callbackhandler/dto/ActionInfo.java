package br.com.bradesco.bpw.callbackhandler.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionInfo {
    private int capturedRuleNum;
    private int rulesSnapshotId;
    private boolean byGlobalPolicy;
    private boolean byRule;
    private String capturedRule;
}