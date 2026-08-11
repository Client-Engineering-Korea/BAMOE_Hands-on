package org.acme.dmn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@TestPropertySource(properties = {
    "kogito.service.url=http://localhost:8080",
    "kogito.jobs-service.url=http://localhost:8080",
    "kogito.data-index.url=http://localhost:8080"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractCaseApiTest {

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    private static final boolean RECORD = Boolean.getBoolean("test.record");

    @LocalServerPort
    private int port;

    /** 리소스 디렉터리명. 예: "case01" */
    protected abstract String caseDir();

    /** DMN 엔드포인트. 예: "/Case01ServiceStatusChange" */
    protected abstract String endpoint();

    /** 요청 본문 구성. 입력 구조가 다른 Case만 재정의 */
    protected String buildBody(JsonNode caseNode) {
        return "{\"request\":" + caseNode.get("request").toString() + "}";
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    /** cases.json의 루트에서 케이스 배열을 추출. DMN은 루트가 곧 배열 */
    protected JsonNode caseNodes(JsonNode root) {
        return root;
    }    

    Stream<Arguments> cases() throws Exception {
        String resource = "/" + caseDir() + "/cases.json";
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("케이스 파일 없음: " + resource);
            JsonNode root = MAPPER.readTree(in);
            List<Arguments> list = new ArrayList<>();
            for (JsonNode node : caseNodes(root)) {      // ← 변경
                list.add(Arguments.of(node.get("case").asText(), buildBody(node)));
            }
            return list.stream();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void verify(String caseId, String body) throws Exception {

        String actual = given()
                .contentType(ContentType.JSON)
                .body(body)
            .when()
                .post(endpoint())
            .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().body().asString();

        Path expectedFile =
            Path.of("src/test/resources", caseDir(), "expected", caseId + ".json");

        if (RECORD) {
            Files.createDirectories(expectedFile.getParent());
            String pretty = MAPPER.writerWithDefaultPrettyPrinter()
                                  .writeValueAsString(MAPPER.readTree(actual));
            Files.writeString(expectedFile, pretty);
            System.out.printf("[RECORDED] %s%n%s%n", expectedFile, pretty);
            return;
        }

        if (!Files.exists(expectedFile)) {
            fail("기대값 파일이 없습니다: " + expectedFile
               + " → mvn test -Dtest.record=true 로 먼저 기록하십시오.");
        }

        JSONAssert.assertEquals(
            Files.readString(expectedFile), actual, JSONCompareMode.STRICT);
    }
}