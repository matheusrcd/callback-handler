package br.com.bradesco.bpw.callbackhandler.dto;

import lombok.*;

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
}
