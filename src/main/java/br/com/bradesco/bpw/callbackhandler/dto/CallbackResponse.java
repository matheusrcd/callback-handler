package br.com.bradesco.bpw.callbackhandler.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class CallbackResponse {
    @NonNull
    private String action;
    @NonNull
    private String requestId;
    private String rejectionReason;

    public Claims getCallbackResponseAsClaims(){
        Claims claimsList = Jwts.claims();
        claimsList.put("action", this.action);
        claimsList.put("requestId", this.requestId);
        claimsList.put("rejectionReason", this.rejectionReason);
        return claimsList;
    }
}
