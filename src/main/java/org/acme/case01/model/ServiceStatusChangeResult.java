package org.acme.case01.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Case 01: 서비스 상태 변경 권한 체크 — API 출력 DTO
 *
 * DMN finalResult Decision의 출력값을 클라이언트에 반환한다.
 *
 * 시나리오별 출력 예시:
 *   1-1 ~ 1-3: { "auth_result": "GRANTED", "business_result": "OK" }
 *   1-4, 1-5:  { "auth_result": "SKIP",    "business_result": "OK" }
 *   1-6:       { "auth_result": "DENIED",  "business_result": "ERROR",
 *                "error_code": "E_AUTH_DENIED", "error_message": "차량 eSIM 처리 권한 없음" }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceStatusChangeResult {

    /**
     * 권한 체크 결과
     * 값: "GRANTED" | "DENIED" | "SKIP" | "AUTH_ERROR"
     */
    @JsonProperty("auth_result")
    private String authResult;

    /**
     * 최종 업무 처리 결과
     * 값: "OK" | "ERROR"
     */
    @JsonProperty("business_result")
    private String businessResult;

    /**
     * 에러 구분 코드 (에러 시에만 포함)
     * 값: "E_AUTH_DENIED" | "E_AUTH_ERROR" | "E_PROMO_BLOCK"
     */
    @JsonProperty("error_code")
    private String errorCode;

    /**
     * 사용자 노출 메시지 (에러 시에만 포함)
     */
    @JsonProperty("error_message")
    private String errorMessage;

    public ServiceStatusChangeResult() {}

    public String getAuthResult() {
        return authResult;
    }

    public void setAuthResult(String authResult) {
        this.authResult = authResult;
    }

    public String getBusinessResult() {
        return businessResult;
    }

    public void setBusinessResult(String businessResult) {
        this.businessResult = businessResult;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "ServiceStatusChangeResult{authResult='" + authResult
                + "', businessResult='" + businessResult
                + "', errorCode='" + errorCode
                + "', errorMessage='" + errorMessage + "'}";
    }
}
