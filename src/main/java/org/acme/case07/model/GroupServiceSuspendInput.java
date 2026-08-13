package org.acme.case07.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Case 07: 그룹사 회선 정지 — 화면/전문 입력 오브젝트
 *
 * 원천 시스템 표기를 그대로 따르므로 필드명이 snake_case이다.
 *
 * JSON 표현:
 * {
 *   "svc_st_chg_cd": "F1",
 *   "svc_chg_rsn_cd": "01"
 * }
 */
public class GroupServiceSuspendInput {

    /**
     * 서비스 상태 변경 코드
     * "F1"(정지)일 때만 조건 대상. 그 외 값은 conditionCheck에서 STOP으로 처리됨
     */
    @JsonProperty("svc_st_chg_cd")
    private String svcStChgCd;

    /**
     * 서비스 변경 사유 코드
     * 대상 값: "01","31","16","21","22","23","24","27","32"
     * zero-padding이 있으므로 반드시 문자열이어야 한다. 숫자 1은 "01"과 매칭되지 않는다.
     */
    @JsonProperty("svc_chg_rsn_cd")
    private String svcChgRsnCd;

    public GroupServiceSuspendInput() {}

    public GroupServiceSuspendInput(String svcStChgCd, String svcChgRsnCd) {
        this.svcStChgCd  = svcStChgCd;
        this.svcChgRsnCd = svcChgRsnCd;
    }

    public String getSvcStChgCd() { return svcStChgCd; }
    public void setSvcStChgCd(String svcStChgCd) { this.svcStChgCd = svcStChgCd; }

    public String getSvcChgRsnCd() { return svcChgRsnCd; }
    public void setSvcChgRsnCd(String svcChgRsnCd) { this.svcChgRsnCd = svcChgRsnCd; }

    @Override
    public String toString() {
        return "GroupServiceSuspendInput{svc_st_chg_cd='" + svcStChgCd
                + "', svc_chg_rsn_cd='" + svcChgRsnCd + "'}";
    }
}
