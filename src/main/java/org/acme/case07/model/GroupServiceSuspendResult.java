package org.acme.case07.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Case 07: 그룹사 회선 정지 권한 체크 — API 출력 DTO
 *
 * DMN finalResult Decision의 출력값이다.
 * 필드명은 DMN itemDefinition(GroupServiceSuspendResult)과 정확히 일치해야 한다.
 *
 * 시나리오별 출력:
 *   7-1, 7-4:            { "authResult": "GRANTED", "businessResult": "OK" }
 *   7-3, 7-5, 7-6, 7-7:  { "authResult": "SKIP",    "businessResult": "OK" }
 *   7-2:                 { "authResult": "DENIED",  "businessResult": "ERROR",
 *                          "errorCode": "E_GRP_SVC_DENIED",
 *                          "errorMessage": "그룹사 회선 정지 불가" }
 *   7-8:                 { "authResult": "AUTH_ERROR", "businessResult": "ERROR",
 *                          "errorCode": "E_AUTH_ERROR",
 *                          "errorMessage": "권한 체크 중 오류" }
 */
public class GroupServiceSuspendResult {

    /**
     * 권한 체크 결과
     * "GRANTED"    — 권한 확인 완료, 정지 가능
     * "DENIED"     — 권한 없음
     * "AUTH_ERROR" — 권한 API 장애 또는 권한 결과 누락
     * "SKIP"       — 조건 미해당(그룹사 회선 0건 등)으로 권한 체크 생략
     */
    @JsonProperty("authResult")
    private String authResult;

    /**
     * 최종 업무 처리 결과 — "OK" | "ERROR"
     *
     * SKIP은 에러가 아니라 "체크 불필요"이므로 businessResult는 OK다.
     */
    @JsonProperty("businessResult")
    private String businessResult;

    /**
     * 에러 구분 코드 (에러 시에만 값이 있음)
     * "E_GRP_SVC_DENIED" | "E_AUTH_ERROR"
     */
    @JsonProperty("errorCode")
    private String errorCode;

    /**
     * 사용자 노출 메시지 (에러 시에만 값이 있음)
     */
    @JsonProperty("errorMessage")
    private String errorMessage;

    public GroupServiceSuspendResult() {}

    public String getAuthResult() { return authResult; }
    public void setAuthResult(String authResult) { this.authResult = authResult; }

    public String getBusinessResult() { return businessResult; }
    public void setBusinessResult(String businessResult) { this.businessResult = businessResult; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        return "GroupServiceSuspendResult{authResult='" + authResult
                + "', businessResult='" + businessResult
                + "', errorCode='" + errorCode
                + "', errorMessage='" + errorMessage + "'}";
    }
}
