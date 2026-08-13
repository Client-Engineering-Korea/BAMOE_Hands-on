package org.acme.case07.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Case 07: 외부 시스템 조회 결과
 *
 * API: zord_grp_co_svc_spc_s0001
 *
 * Case01의 {@code org.acme.common.model.ExtInquiryResult}와 같은 역할이지만,
 * 케이스마다 필드를 공용 DTO에 누적하면 관리가 어려워지므로 Case07은 전용 클래스로 분리한다.
 *
 * JSON 표현:
 * {
 *   "grp_co_svc_cnt": 2
 * }
 */
public class GroupServiceSuspendExtInquiryResult {

    /**
     * 그룹사 회선 수 (zord_grp_co_svc_spc_s0001)
     *
     * 0이면 그룹사 회선이 없다는 뜻이므로 권한 체크 자체가 불필요하다 → conditionCheck STOP.
     * 1건 이상일 때만 정지 권한을 확인한다.
     *
     * Integer(nullable)로 둔 이유: API 무응답 시 null이 들어올 수 있으며,
     * DMN은 이 경우에도 conditionCheck STOP으로 안전하게 떨어진다.
     */
    @JsonProperty("grp_co_svc_cnt")
    private Integer grpCoSvcCnt;

    public GroupServiceSuspendExtInquiryResult() {}

    public GroupServiceSuspendExtInquiryResult(Integer grpCoSvcCnt) {
        this.grpCoSvcCnt = grpCoSvcCnt;
    }

    public Integer getGrpCoSvcCnt() { return grpCoSvcCnt; }
    public void setGrpCoSvcCnt(Integer grpCoSvcCnt) { this.grpCoSvcCnt = grpCoSvcCnt; }

    @Override
    public String toString() {
        return "GroupServiceSuspendExtInquiryResult{grp_co_svc_cnt=" + grpCoSvcCnt + "}";
    }
}
