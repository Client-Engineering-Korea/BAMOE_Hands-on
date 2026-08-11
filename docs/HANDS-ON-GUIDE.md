# BAMOE Hands-on Guide — Decision | Maven | Spring Boot

## 이번 핸즈온의 최종 목표

가상의 협업 시나리오를 통해, **BAMOE로 Decision/Rule을 만들고 GitHub 중심으로 검토·승인한 뒤 OpenShift에 배포하는 바탕**을 경험합니다.

```text
각자 브랜치에서 Rule 구현·테스트
        ↓
   GitHub Pull Request
        ↓
 발표자(admin) 검토·승인 (merge)
        ↓
 발표자(admin)가 승인된 main으로 OpenShift S2I 배포
        ↓
 공유 Route로 서비스 호출·검증
```

| 단계 | 담당 | 내용 |
|------|------|------|
| 1. 환경 설정 | 참가자 | JDK / Maven / 레포 clone / 로컬 기동 |
| 2. Rule 구현 | 참가자 | 본인 브랜치에서 DMN, DRL |
| 3. Rule 테스트 | 참가자 | JUnit, scesim → push → **PR** |
| 4. 검토·배포·검증 | **발표자** | PR 승인 → S2I 배포 → URL 공유 / 참가자는 호출 |

이 클러스터는 IBM TechZone **IBMid**만 지원하므로, OpenShift 배포 권한은 발표자(admin)만 사용합니다.  
참가자의 협업 접점은 **GitHub(브랜치·PR)** 이고, 배포는 **승인된 코드의 운영 반영**으로 시연합니다.

accelerator: **Decisions (Spring Boot + Maven)**  
배포: **로컬 JAR + OpenShift S2I binary** (`scripts/deploy-s2i.sh`)

---

## 협업 시나리오 (스토리라인)

### 전제

- 공통 GitHub 저장소 1개 (Hands-on용 Business Service)
- `main`(또는 `develop`) = 배포 가능한 기준선
- 참가자 = 기여자 / 발표자 = 리뷰어 + 배포 담당

### 흐름

1. **공동 작업의 기준은 GitHub repo**  
   모든 Rule·테스트·설정 변경은 커밋으로 남긴다.

2. **각자 브랜치에서 작업 후 PR**  
   예: `feature/<이름>-insurance-dmn`  
   PR 설명에 변경한 DMN/DRL, 테스트 결과(`mvn test`)를 적는다.

3. **발표자가 PR 검토·승인**  
   코드·시나리오 테스트·Swagger 로컬 확인 후 merge.

4. **발표자가 승인된 내용으로 배포**  
   merge된 `main`을 pull → `./scripts/deploy-s2i.sh` → Route 공유.

5. **팀이 배포 결과 검증**  
   참가자는 공유 URL로 Decision/Rule API를 호출한다.

→ *“BAMOE로 규칙을 만들고, Git 협업(PR)과 배포(OpenShift)를 잇는 바탕”* 이 이번 세션의 마지막 메시지입니다.

---

## 0. 발표자 사전 준비

- [ ] GitHub 조직/저장소 생성, 참가자 write(또는 fork+PR) 권한
- [ ] 기본 브랜치 보호(선택): PR 필수, 발표자 approve 후 merge
- [ ] 샘플 DMN/DRL·테스트 skeleton이 있는 초기 커밋
- [ ] OpenShift 데모 프로젝트 (예: `bamoe-demo`) + admin 로그인
- [ ] S2I builder ImageStream 확인 (이 클러스터: `openshift/java:openjdk-17-ubi8`)
- [ ] 세션 전 `./scripts/deploy-s2i.sh` 1회 성공
- [ ] 참가자용: clone URL, 브랜치 네이밍, PR 템플릿(간단)

```bash
oc login --web https://api.<CLUSTER>:6443
oc new-project bamoe-demo    # 최초 1회
oc project bamoe-demo
```

---

## 1. 환경 설정 (참가자)

### 로컬 도구

- JDK **17**, Maven 3.9+
- Git / GitHub 계정
- (권장) VS Code + BAMOE Developer Tools

> 참가자에게 OpenShift `oc` 배포 권한은 필요 없습니다.

### 저장소 clone 및 기동

```bash
git clone <HANDS-ON_REPO_URL>
cd BMAOE_Hands-on   # 실제 디렉터리명에 맞게

git checkout -b feature/<your-name>-rules

mvn clean compile spring-boot:run
```

- Swagger: http://localhost:8080/swagger-ui/index.html  

**완료 기준:** 로컬 Swagger UI 확인 + 본인 feature 브랜치 생성

---

## 2. Rule 구현 (DMN, DRL) (참가자 · 브랜치)

1. `src/main/resources/` 에 DMN / DRL 추가·수정  
2. 필요 시 `src/main/java/` 모델 추가  
3. 로컬 기동 후 Swagger에서 엔드포인트·샘플 요청 확인  

```bash
mvn clean compile spring-boot:run
```

**완료 기준:** Swagger에 본인 Rule/Decision 엔드포인트가 보이고, 샘플 요청이 성공한다. >> 본인 규칙이 로컬에서 동작한다.

---

## 3. Rule 테스트 → PR (참가자)

```bash
mvn clean test
```

통과 후:

```bash
git add .
git commit -m "Add <rule/decision> and tests"
git push -u origin HEAD

# GitHub에서 Pull Request 생성 → base: main
```

PR에 적을 내용 예시:

- 변경한 파일 (DMN/DRL/scesim)
- `mvn clean test` 결과
- 로컬에서 확인한 API 경로

**완료 기준:** PR이 열리고 리뷰 대기 상태

---

## 4. 협업 시나리오 (배포 데모 + 참가자 호출)

### 4-1. PR 검토·승인

- 변경 diff / 테스트 / (필요 시) 로컬 실행 확인  
- Approve 후 **main에 merge**

### 4-2. 승인된 main으로 OpenShift 배포

```bash
git checkout main
git pull

./scripts/deploy-s2i.sh
# 재배포만: ./scripts/deploy-s2i.sh --rebuild
```

스크립트: `mvn package` → S2I `--from-file` → Deployment / Route

```bash
./scripts/deploy-s2i.sh --url
./scripts/deploy-s2i.sh --status
```

### 4-3. 팀 검증 (참가자)

발표자가 Route / Swagger URL을 공유하면:

```bash
export ROUTE_HOST=<route-host>
curl -k -I "https://$ROUTE_HOST/swagger-ui/index.html"
# curl -k -X POST "https://$ROUTE_HOST/<path>" -H 'Content-Type: application/json' -d '{...}'
```

**완료 기준:**

- [ ] PR merge 완료  
- [ ] OpenShift Pod Ready + Route 공유  
- [ ] 참가자가 공유 URL로 승인된 Rule API 호출 성공  

---

## 최종 정리

이번 실습으로 확인할 수 있는 것:

1. **BAMOE**로 Decision/Rule Business Service를 표준 Spring Boot 프로젝트로 구현·테스트한다.  
2. **GitHub 브랜치 + PR**으로 규칙을 공동 검토·승인한다.  
3. 승인된 산출물을 발표자(운영 역할)가 **OpenShift S2I**로 배포해 팀이 함께 검증한다.  

→ 실제 조직에서는 여기에 CI(PR 체크), CD(merge 후 자동 배포), 환경별 프로젝트 분리만 더하면 동일한 협업 바탕을 확장할 수 있다.

---

## 문제 해결

| 증상 | 확인 |
|------|------|
| PR 충돌 | `main` rebase/merge 후 재 push |
| `mvn test` 실패 | scesim/JUnit·모델 불일치 |
| JAR not found | `target/bamoe-rule-service.jar` |
| 참가자 Route 접속 실패 | URL·방화벽·인증서(`curl -k`) |

---

## 관련 문서

- 강사 배포 치트시트: [INSTRUCTOR-DEPLOY-S2I.md](INSTRUCTOR-DEPLOY-S2I.md)
- 스크립트: `scripts/deploy-s2i.sh`

