package org.acme.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 외부 시스템(서비스 조회) 결과 공통 DTO
 *
 * 여러 use case에서 공통으로 사용하는 extInquiryResult 오브젝트.
 * use case별로 필요한 필드를 추가하여 활용한다.
 *
 * 현재 사용 필드:
 *   Case01: svcDtlClCd, promo
 *   Case02: svcCd, svcTypCd
 *
 * JSON 표현 (Case01):
 * {
 *   "svcDtlClCd": "CA",
 *   "promo": 0
 * }
 *
 * JSON 표현 (Case02):
 * {
 *   "svcCd": "P",
 *   "svcTypCd": "72"
 * }
 */
public class ExtInquiryResult {

    // -------------------------------------------------------------------------
    // Case01 — 서비스 상태 변경 (processServiceStatusChange)
    // -------------------------------------------------------------------------

    /**
     * DB 조회값: zord_svc_s4253.svc_dtl_cl_cd (Case01)
     * "CA": 차량 eSIM 서비스 → ConditionCheck PROCEED
     * 그 외: STOP
     */
    @JsonProperty("svcDtlClCd")
    private String svcDtlClCd;

    /**
     * DB 조회값: zord_svc_prod_s0425 프로모션 카운트 (Case01)
     * svcStChgCd == "F1" 일 때만 의미 있음. 그 외 코드는 0으로 채움.
     */
    @JsonProperty("promo")
    private int promo;

    // -------------------------------------------------------------------------
    // Case02 — 유선 명의변경 (processWirelineNameChange)
    // -------------------------------------------------------------------------

    /**
     * API 조회값: zordmb07s0100.svc_cd (Case02)
     * "P": 별정서비스 → ConditionCheck PROCEED (svcTypCd와 AND 조건)
     * 그 외: STOP
     */
    @JsonProperty("svcCd")
    private String svcCd;

    /**
     * API 조회값: zordmb07s0100.svc_typ_cd (Case02)
     * "72": 별정 유형 → ConditionCheck PROCEED (svcCd와 AND 조건)
     * 그 외: STOP
     */
    @JsonProperty("svcTypCd")
    private String svcTypCd;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ExtInquiryResult() {}

    /** Case01용 생성자 */
    public ExtInquiryResult(String svcDtlClCd, int promo) {
        this.svcDtlClCd = svcDtlClCd;
        this.promo      = promo;
    }

    /** Case02용 생성자 */
    public ExtInquiryResult(String svcCd, String svcTypCd, boolean isCase02Marker) {
        this.svcCd    = svcCd;
        this.svcTypCd = svcTypCd;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters — Case01
    // -------------------------------------------------------------------------

    public String getSvcDtlClCd() { return svcDtlClCd; }
    public void setSvcDtlClCd(String svcDtlClCd) { this.svcDtlClCd = svcDtlClCd; }

    public int getPromo() { return promo; }
    public void setPromo(int promo) { this.promo = promo; }

    // -------------------------------------------------------------------------
    // Getters / Setters — Case02
    // -------------------------------------------------------------------------

    public String getSvcCd() { return svcCd; }
    public void setSvcCd(String svcCd) { this.svcCd = svcCd; }

    public String getSvcTypCd() { return svcTypCd; }
    public void setSvcTypCd(String svcTypCd) { this.svcTypCd = svcTypCd; }

    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ExtInquiryResult{"
                + "svcDtlClCd='" + svcDtlClCd + '\''
                + ", promo=" + promo
                + ", svcCd='" + svcCd + '\''
                + ", svcTypCd='" + svcTypCd + '\''
                + '}';
    }
}
