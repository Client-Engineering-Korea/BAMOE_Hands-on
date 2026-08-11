package org.acme.dmn;

class Case01ApiTest extends AbstractCaseApiTest {
    @Override protected String caseDir()  { return "case01"; }
    @Override protected String endpoint() { return "/Case01ServiceStatusChange"; }
}