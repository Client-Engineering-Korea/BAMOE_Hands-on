package org.acme.case07.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Case 07: 그룹사 회선 정지 권한 체크 — API 입력 DTO
 *
 * 업무: 그룹사 회선을 보유한 가입자의 정지(F1) 요청 시 담당자 권한을 확인한다.
 *       그룹사 회선이 0건이면 확인할 대상이 없으므로 권한 체크를 생략한다.
 *
 * 주의 — 이 클래스는 문서·IDE 편의용이다.
 * 런타임 계약은 DMN의 itemDefinition(GroupServiceSuspendRequest)이며,
 * 생성된 REST 리소스는 Map&lt;String,Object&gt;로 요청을 받는다.
 * 필드를 여기에만 추가하면 룰에는 아무 영향이 없다.
 *
 * 실제 요청 본문 (request로 한 겹 감싼다):
 * {
 *   "request": {
 *     "authId": "ORDOP1026",
 *     "checkType": "SINGLE",
 *     "userId": "USER_FULL",
 *     "svcMgmtNum": 1000000020,
 *     "input": {
 *       "svc_st_chg_cd": "F1",
 *       "svc_chg_rsn_cd": "01"
 *     },
 *     "extInquiryResult": {
 *       "grp_co_svc_cnt": 2
 *     },
 *     "authResult": "GRANTED"
 *   }
 * }
 *
 * DMN 룰이 실제로 참조하는 값은 4개뿐이다:
 *   input.svc_st_chg_cd                → conditionCheck
 *   input.svc_chg_rsn_cd               → conditionCheck
 *   extInquiryResult.grp_co_svc_cnt    → conditionCheck
 *   authResult                         → authResultCheck
 *
 * authId / checkType / userId / svcMgmtNum은 호출 컨텍스트를 남기기 위한
 * pass-through 필드이며 어떤 룰도 참조하지 않는다.
 */
public class GroupServiceSuspendRequest {

    /**
     * 권한 코드. Case07은 "ORDOP1026".
     * 애플리케이션이 권한 API를 호출할 때 사용하며, DMN 룰에서는 사용하지 않는다.
     */
    @JsonProperty("authId")
    private String authId;

    /**
     * 권한 체크 유형. 예: "SINGLE"
     * 룰에서 사용하지 않는다.
     */
    @JsonProperty("checkType")
    private String checkType;

    /**
     * 처리 담당자 ID. 권한 API 호출 시 사용하며 DMN 룰에서는 사용하지 않는다.
     * (권한 판정 결과는 authResult로 받는다)
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * 서비스 관리 번호 (식별자). 룰에서 사용하지 않는다.
     * Case01은 string이지만 Case07은 원천 표기를 따라 number다.
     */
    @JsonProperty("svcMgmtNum")
    private Long svcMgmtNum;

    /**
     * 화면/전문 입력값 — svc_st_chg_cd, svc_chg_rsn_cd
     */
    @JsonProperty("input")
    private GroupServiceSuspendInput input;

    /**
     * 외부 조회 결과 — grp_co_svc_cnt (zord_grp_co_svc_spc_s0001)
     */
    @JsonProperty("extInquiryResult")
    private GroupServiceSuspendExtInquiryResult extInquiryResult;

    /**
     * 권한 API 결과 — "GRANTED" | "DENIED" | "ERROR"
     *
     * DMN이 권한 API를 호출하는 것이 아니라, 애플리케이션이 authId/userId/checkType으로
     * 호출한 결과를 넣어주면 DMN은 그것을 해석만 한다.
     * conditionCheck가 STOP이면 이 값과 무관하게 최종 authResult는 "SKIP"이 된다.
     */
    @JsonProperty("authResult")
    private String authResult;

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public String getAuthId() { return authId; }
    public void setAuthId(String authId) { this.authId = authId; }

    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getSvcMgmtNum() { return svcMgmtNum; }
    public void setSvcMgmtNum(Long svcMgmtNum) { this.svcMgmtNum = svcMgmtNum; }

    public GroupServiceSuspendInput getInput() { return input; }
    public void setInput(GroupServiceSuspendInput input) { this.input = input; }

    public GroupServiceSuspendExtInquiryResult getExtInquiryResult() { return extInquiryResult; }
    public void setExtInquiryResult(GroupServiceSuspendExtInquiryResult extInquiryResult) {
        this.extInquiryResult = extInquiryResult;
    }

    public String getAuthResult() { return authResult; }
    public void setAuthResult(String authResult) { this.authResult = authResult; }
}
