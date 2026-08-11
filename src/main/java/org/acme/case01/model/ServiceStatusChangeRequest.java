package org.acme.case01.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.common.model.ExtInquiryResult;

/**
 * Case 01: 서비스 상태 변경 권한 체크 — API 입력 DTO
 *
 * 실제 Rule Engine에 전달되는 JSON 구조를 그대로 반영한다.
 *
 * {
 *   "svcMgmtNum": "1000000001",
 *   "svcStChgCd": "F1",
 *   "extInquiryResult": {
 *     "svcDtlClCd": "CA",
 *     "promo": 0
 *   },
 *   "authResult": "GRANTED",
 *   "periodLevel": null
 * }
 *
 * DMN inputData 매핑:
 *   svcStChgCd                       → DMN: "svcStChgCd"   (서비스 상태 변경 코드)
 *   extInquiryResult.svcDtlClCd      → DMN: "svcDtlClCd"   (DB 조회 결과)
 *   extInquiryResult.promo           → DMN: "promo"         (프로모션 카운트)
 *   authResult                       → DMN: "authResult"
 *   periodLevel                      → DMN: "periodLevel"
 *
 * 테스트 케이스 매핑 (1-1 ~ 1-6):
 *   1-1: svcDtlClCd=CA, svcStChgCd=F1, authResult=GRANTED, promo=0  → OK
 *   1-2: svcDtlClCd=CA, svcStChgCd=F2, authResult=GRANTED, promo=0  → OK
 *   1-3: svcDtlClCd=CA, svcStChgCd=FR, authResult=GRANTED, promo=0  → OK
 *   1-4: svcDtlClCd=CA, svcStChgCd=XX, authResult=GRANTED, promo=0  → OK (SKIP)
 *   1-5: svcDtlClCd=AA, svcStChgCd=F1, authResult=GRANTED, promo=0  → OK (SKIP)
 *   1-6: svcDtlClCd=CA, svcStChgCd=F1, authResult=DENIED,  promo=0  → ERROR
 */
public class ServiceStatusChangeRequest {

    /**
     * 서비스 관리 번호 (식별자)
     */
    @JsonProperty("svcMgmtNum")
    private String svcMgmtNum;

    /**
     * 서비스 상태 변경 코드 — DMN 직접 입력 변수
     * 허용 값: "F1" (정지), "F2" (해제), "FR" (강제정지)
     * 그 외 값(예: "XX")은 ConditionCheck에서 STOP으로 처리됨
     */
    @JsonProperty("svcStChgCd")
    private String svcStChgCd;

    /**
     * 외부 조회 결과: svcDtlClCd + promo를 묶는 중첩 객체
     */
    @JsonProperty("extInquiryResult")
    private ExtInquiryResult extInquiryResult;

    /**
     * 권한 API 결과
     * "GRANTED" | "DENIED" | "ERROR"
     */
    @JsonProperty("authResult")
    private String authResult;

    /**
     * 기간 등급 (nullable)
     * svcStChgCd == "F1" 일 때 사용. 해당 없으면 null.
     */
    @JsonProperty("periodLevel")
    private Integer periodLevel;

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public String getSvcMgmtNum() { return svcMgmtNum; }
    public void setSvcMgmtNum(String svcMgmtNum) { this.svcMgmtNum = svcMgmtNum; }

    public String getSvcStChgCd() { return svcStChgCd; }
    public void setSvcStChgCd(String svcStChgCd) { this.svcStChgCd = svcStChgCd; }

    public ExtInquiryResult getExtInquiryResult() { return extInquiryResult; }
    public void setExtInquiryResult(ExtInquiryResult extInquiryResult) { this.extInquiryResult = extInquiryResult; }

    public String getAuthResult() { return authResult; }
    public void setAuthResult(String authResult) { this.authResult = authResult; }

    public Integer getPeriodLevel() { return periodLevel; }
    public void setPeriodLevel(Integer periodLevel) { this.periodLevel = periodLevel; }
}
