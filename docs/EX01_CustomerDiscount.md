# EX01 — CustomerDiscount (Pure DMN)

순수 DMN만으로 구현한 고객 등급별 할인율 결정 예제입니다.  
Java DTO 없이 FEEL 타입과 Decision Table만 사용합니다.

---

## 1. 예제 시나리오

### 비즈니스 배경

온라인 쇼핑몰에서 고객 등급(`customerCategory`)과 주문 금액(`orderAmount`)을 보고  
적용할 할인율(`discountPercent`)을 자동으로 결정합니다.

### 입력

| 필드 | 타입 | 설명 |
|------|------|------|
| `customerCategory` | string | `Bronze` / `Silver` / `Gold` |
| `orderAmount` | number | 주문 금액 |

### 출력

| 필드 | 타입 | 설명 |
|------|------|------|
| `discountPercent` | number | 할인율 (%) |

### 결정 규칙 (Decision Table, hitPolicy = FIRST)

| 등급 | 주문 금액 | 할인율 |
|------|-----------|--------|
| Gold | ≥ 100,000 | **15%** |
| Gold | (그 외) | **10%** |
| Silver | ≥ 100,000 | **10%** |
| Silver | (그 외) | **5%** |
| Bronze | ≥ 100,000 | **5%** |
| (그 외) | (그 외) | **0%** |

### 관련 파일

| 구분 | 경로 |
|------|------|
| DMN | `src/main/resources/dmn/EX01_CustomerDiscount.dmn` |
| 테스트 JSON | `src/test/resources/json/EX01_CustomerDiscount.cases.json` |
| Java | 없음 (Pure DMN) |

### REST 엔드포인트

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/EX01_CustomerDiscount` | 할인율 평가 |
| `POST` | `/EX01_CustomerDiscount/dmnresult` | 상세 DMN 결과(평가 트레이스 포함) |
| `GET` | `/EX01_CustomerDiscount` | DMN 모델 XML |

---

## 2. 실행 방법

### 사전 조건

- JDK 17+
- Maven 3.9+

### 서비스 기동

프로젝트 루트에서:

```bash
mvn clean spring-boot:run
```

기동 후 확인:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Actuator: http://localhost:8080/actuator/health

> `kogito-maven-plugin`의 코드 생성은 `process-classes` 단계에서 수행됩니다.  
> `spring-boot:run` / `package` 사용 시 자동으로 포함됩니다.

### 단일 요청 실행

```bash
curl -s -X POST http://localhost:8080/EX01_CustomerDiscount \
  -H 'Content-Type: application/json' \
  -d '{"customerCategory":"Gold","orderAmount":150000}'
```

예상 응답:

```json
{
  "customerCategory": "Gold",
  "orderAmount": 150000,
  "discountPercent": 15
}
```

### Swagger에서 실행

1. http://localhost:8080/swagger-ui/index.html 접속
2. `EX01_CustomerDiscount` 관련 API 선택
3. `POST /EX01_CustomerDiscount` → Try it out
4. Request body에 JSON 입력 후 Execute

---

## 3. 테스트 방법

### 테스트 케이스 파일

`src/test/resources/json/EX01_CustomerDiscount.cases.json`

| ID | 시나리오 | 기대 `discountPercent` |
|----|----------|------------------------|
| EX01-01 | Gold + 고액(150000) | 15 |
| EX01-02 | Gold + 일반(50000) | 10 |
| EX01-03 | Silver + 고액(120000) | 10 |
| EX01-04 | Silver + 일반(30000) | 5 |
| EX01-05 | Bronze + 고액(100000) | 5 |
| EX01-06 | Bronze + 일반(20000) | 0 |

### curl로 케이스별 검증

서비스가 떠 있는 상태에서:

```bash
# EX01-01
curl -s -X POST http://localhost:8080/EX01_CustomerDiscount \
  -H 'Content-Type: application/json' \
  -d '{"customerCategory":"Gold","orderAmount":150000}'

# EX01-02
curl -s -X POST http://localhost:8080/EX01_CustomerDiscount \
  -H 'Content-Type: application/json' \
  -d '{"customerCategory":"Gold","orderAmount":50000}'

# EX01-06 (기본 0%)
curl -s -X POST http://localhost:8080/EX01_CustomerDiscount \
  -H 'Content-Type: application/json' \
  -d '{"customerCategory":"Bronze","orderAmount":20000}'
```

### JSON 파일에서 request만 추출해 호출

```bash
# 첫 번째 케이스 request 사용
jq -c '.cases[0].request' src/test/resources/json/EX01_CustomerDiscount.cases.json \
  | curl -s -X POST http://localhost:8080/EX01_CustomerDiscount \
      -H 'Content-Type: application/json' -d @-
```

모든 케이스를 순회:

```bash
jq -c '.cases[]' src/test/resources/json/EX01_CustomerDiscount.cases.json | while read -r case; do
  id=$(echo "$case" | jq -r '.id')
  name=$(echo "$case" | jq -r '.name')
  echo "=== $id: $name ==="
  echo "$case" | jq -c '.request' \
    | curl -s -X POST http://localhost:8080/EX01_CustomerDiscount \
        -H 'Content-Type: application/json' -d @-
  echo
done
```

### 결과 확인 포인트

- 응답 JSON에 `discountPercent`가 케이스의 `expected.discountPercent`와 일치하는지 확인합니다.
- Decision Table은 `FIRST` hit policy이므로, 위에서부터 첫 번째 매칭 규칙만 적용됩니다.

### (선택) DMN 상세 결과

규칙 매칭 과정을 보고 싶을 때:

```bash
curl -s -X POST http://localhost:8080/EX01_CustomerDiscount/dmnresult \
  -H 'Content-Type: application/json' \
  -d '{"customerCategory":"Gold","orderAmount":150000}' | jq .
```
