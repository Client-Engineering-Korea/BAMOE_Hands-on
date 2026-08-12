# BAMOE Hands-on — Decision Rule Service

> BAMOE Canvas Accelerator **Decisions (Spring Boot + Maven)** (`9.5.1-ibm-0002`) 기반으로 생성된 Business Service입니다.  
> DMN Decision/Rule을 Spring Boot로 서빙하고, GitHub PR 협업 후 OpenShift S2I로 배포하는 핸즈온용 저장소입니다.

| 항목 | 값 |
|------|-----|
| Artifact | `org.acme:bamoe-rule-service:1.0.0-SNAPSHOT` |
| JDK | 17 |
| Spring Boot | 4.0.7 |
| BAMOE | 9.5.1-ibm-0002 |
| Accelerator | Decisions (Spring Boot + Maven) |

---

## Description

가상의 협업 시나리오에서 **BAMOE로 Decision/Rule을 만들고**, GitHub 브랜치·PR로 검토·승인한 뒤, 발표자(admin)가 승인된 `main`을 OpenShift에 배포하는 흐름을 경험합니다.

포함 예제:

| 예제 | DMN | 설명 |
|------|-----|------|
| **EX01** | `EX01_CustomerDiscount` | Pure DMN — 고객 등급·주문 금액 → 할인율 |
| **Case01** | `Case01ServiceStatusChange` | Java DTO + DMN — 서비스 상태 변경 권한 체크 |

상세 가이드:

- 핸즈온 전체 흐름: [docs/HANDS-ON-GUIDE.md](docs/HANDS-ON-GUIDE.md)
- EX01 예제: [docs/EX01_CustomerDiscount.md](docs/EX01_CustomerDiscount.md)

---

## Project layout

```text
├── docs/                          # 핸즈온·예제 문서
├── scripts/
│   └── deploy-s2i.sh              # OpenShift S2I binary 배포 (발표자)
├── src/main/
│   ├── java/org/acme/
│   │   ├── BamoeSpringBootApplication.java
│   │   ├── BamoeCorsConfig.java
│   │   ├── case01/model/          # Case01 요청·응답 DTO
│   │   └── common/model/          # 공통 모델 (ExtInquiryResult 등)
│   └── resources/
│       ├── application.properties
│       └── dmn/
│           ├── EX01_CustomerDiscount.dmn
│           └── Case01ServiceStatusChange.dmn
└── src/test/
    ├── java/org/acme/dmn/         # REST API 시나리오 테스트
    └── resources/
        ├── Case01/                # Case01 입·출력 fixture
        ├── Case01_test.scesim
        └── json/EX01_CustomerDiscount.cases.json
```

---

## Prerequisites

- JDK **17**, Maven **3.9+**
- Git / GitHub 계정
- (권장) VS Code + BAMOE Developer Tools
- (발표자만) `oc` CLI, OpenShift 프로젝트 접근 권한

BAMOE Maven 저장소는 `pom.xml`의 `repositories` / `pluginRepositories`에 설정되어 있습니다.

---

## Building and running

### Dev mode

```shell
mvn clean compile spring-boot:run
```

기동 후:

| URL | 설명 |
|-----|------|
| http://localhost:8080 | Business Service |
| http://localhost:8080/swagger-ui/index.html | Swagger UI |

`server.address=0.0.0.0` 이므로 동일 네트워크의 다른 호스트에서도 접근할 수 있습니다.

### Package and run

```sh
mvn clean package
java -jar ./target/bamoe-rule-service.jar
```

### Tests

```sh
mvn clean test
```

---

## Decision endpoints

Swagger에서 전체 엔드포인트를 확인할 수 있습니다. 주요 경로:

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/EX01_CustomerDiscount` | 고객 할인율 평가 |
| `GET` | `/EX01_CustomerDiscount` | DMN 모델 XML |
| `POST` | `/Case01ServiceStatusChange` | 서비스 상태 변경 권한 체크 |
| `GET` | `/Case01ServiceStatusChange` | DMN 모델 XML |

EX01 샘플 요청:

```bash
curl -s -X POST http://localhost:8080/EX01_CustomerDiscount \
  -H 'Content-Type: application/json' \
  -d '{"customerCategory":"Gold","orderAmount":150000}'
```

---

## Hands-on session

**Goal:** GitHub에서 브랜치·PR로 Rule을 협업하고, 강사(admin)가 승인된 `main`을 OpenShift S2I로 배포하는 바탕을 경험합니다.

| Who | What |
|-----|------|
| Participants | clone → feature branch → DMN/DRL → `mvn test` → Pull Request |
| Instructor | review/merge PR → `./scripts/deploy-s2i.sh` → share Route URL |

```sh
# Instructor only (after PR merge)
git checkout main && git pull
oc project bamoe-demo
./scripts/deploy-s2i.sh

# 유틸
./scripts/deploy-s2i.sh --rebuild   # 재배포
./scripts/deploy-s2i.sh --url       # Route URL
./scripts/deploy-s2i.sh --status    # build / pod / route
```

전체 절차·체크리스트는 [docs/HANDS-ON-GUIDE.md](docs/HANDS-ON-GUIDE.md)를 참고하세요.

---

## Configuring CORS

기본적으로 모든 origin(`*`)을 허용합니다. `bamoe.cors.allowed-origin-patterns`로 설정합니다.

**IMPORTANT:** 프로덕션 배포 전에는 신뢰할 수 있는 origin만 허용하도록 변경하세요.

1. **`application.properties`**

   ```properties
   bamoe.cors.allowed-origin-patterns=https://example.com
   bamoe.cors.allowed-origin-patterns=https://example.com,https://another.com
   bamoe.cors.allowed-origin-patterns=https://*.example.com
   ```

2. **환경 변수**

   ```bash
   export BAMOE_CORS_ALLOWED_ORIGIN_PATTERNS=https://example.com,https://another.com
   ```

---

## Dev Deployments (Canvas)

BAMOE Canvas에서 Kubernetes/OpenShift로 바로 배포할 때의 매니페스트는 [.bamoe/dev-deployments](.bamoe/dev-deployments/README.md)를 참고하세요.  
핸즈온 세션의 발표자 배포는 `scripts/deploy-s2i.sh`를 사용합니다.

---

## Notes

`src/main/resources/application.properties`에는 다음이 포함됩니다.

- CORS (`bamoe.cors.allowed-origin-patterns`)
- (선택) OpenShift TLS passthrough용 SSL 설정 주석

Maven 표준 레이아웃에 맞춰 확장하세요.

| 경로 | 용도 |
|------|------|
| `src/main/java/` | Java 프로덕션 코드·DTO |
| `src/main/resources/dmn/` | DMN Decision |
| `src/main/resources/` | 설정, DRL, Excel Decision Table(`.xlsx`) 등 |
| `src/test/java/` | JUnit / REST Assured 테스트 |
| `src/test/resources/` | 테스트 fixture, scesim |

IBM BAMOE 9.5.x 공식 문서: [IBM BAMOE Documentation](https://www.ibm.com/docs/en/ibamoe/9.5.x)
