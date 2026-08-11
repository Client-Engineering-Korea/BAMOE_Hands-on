#!/usr/bin/env bash
# IBM BAMOE Hands-on — Spring Boot local package + OpenShift S2I binary deploy
#
# Usage:
#   ./scripts/deploy-s2i.sh              # package + create (if needed) + build + deploy + route
#   ./scripts/deploy-s2i.sh --rebuild    # package + start-build only (existing app)
#   ./scripts/deploy-s2i.sh --status     # show build / pod / route status
#   ./scripts/deploy-s2i.sh --url        # print Route URL
#
# Optional environment variables:
#   APP_NAME          Deployment / BuildConfig name (default: bamoe-rule-service)
#   JAR_FILE          Path to Spring Boot jar (default: target/${APP_NAME}.jar)
#   BUILDER_IS        S2I ImageStream reference
#                     (default: openshift/java:openjdk-17-ubi8)
#   SKIP_TESTS        Pass -DskipTests to Maven when packaging (default: true)
#   CREATE_ROUTE      Create edge Route if missing (default: true)
#   INSECURE_CURL     Use curl -k when printing smoke-test hint (default: true)

set -euo pipefail

APP_NAME="${APP_NAME:-bamoe-rule-service}"
JAR_FILE="${JAR_FILE:-target/${APP_NAME}.jar}"
BUILDER_IS="${BUILDER_IS:-openshift/java:openjdk-17-ubi8}"
SKIP_TESTS="${SKIP_TESTS:-true}"
CREATE_ROUTE="${CREATE_ROUTE:-true}"
INSECURE_CURL="${INSECURE_CURL:-true}"

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

MODE="deploy"
case "${1:-}" in
  --rebuild) MODE="rebuild" ;;
  --status)  MODE="status" ;;
  --url)     MODE="url" ;;
  --help|-h)
    sed -n '2,20p' "$0"
    exit 0
    ;;
  "")
    ;;
  *)
    echo "Unknown option: $1 (try --help)" >&2
    exit 1
    ;;
esac

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "ERROR: '$1' is required but not found in PATH." >&2
    exit 1
  }
}

need_cmd oc
need_cmd mvn

ensure_logged_in() {
  if ! oc whoami >/dev/null 2>&1; then
    echo "ERROR: Not logged in. Run: oc login <API_URL> -u <USER>" >&2
    exit 1
  fi
  echo "OpenShift user : $(oc whoami)"
  echo "Project        : $(oc project -q)"
}

package_jar() {
  echo ""
  echo "==> Maven package (Spring Boot)"
  local mvn_args=(clean package)
  if [[ "${SKIP_TESTS}" == "true" ]]; then
    mvn_args+=(-DskipTests)
  fi
  mvn "${mvn_args[@]}"

  if [[ ! -f "${JAR_FILE}" ]]; then
    echo "ERROR: JAR not found: ${JAR_FILE}" >&2
    echo "Check <finalName> / <artifactId> in pom.xml." >&2
    exit 1
  fi
  echo "JAR ready: ${JAR_FILE} ($(du -h "${JAR_FILE}" | awk '{print $1}'))"
}

buildconfig_exists() {
  oc get buildconfig "${APP_NAME}" >/dev/null 2>&1
}

app_exists() {
  oc get deployment "${APP_NAME}" >/dev/null 2>&1 \
    || oc get deploymentconfig "${APP_NAME}" >/dev/null 2>&1 \
    || oc get svc "${APP_NAME}" >/dev/null 2>&1
}

create_s2i_build() {
  echo ""
  echo "==> Create S2I binary BuildConfig (${APP_NAME}) using builder ${BUILDER_IS}"
  if buildconfig_exists; then
    echo "BuildConfig already exists — skip create"
    return
  fi

  # Prefer ImageStream in 'openshift' namespace; fall back to raw image ref via --image-stream as given.
  if ! oc new-build \
      --name="${APP_NAME}" \
      --binary \
      --strategy=source \
      --image-stream="${BUILDER_IS}"; then
    echo ""
    echo "ERROR: Failed to create BuildConfig with --image-stream=${BUILDER_IS}" >&2
    echo "List candidates, then re-run with an explicit stream, e.g.:" >&2
    echo "  oc new-app -S java" >&2
    echo "  BUILDER_IS=openshift/java:openjdk-17-ubi8 ./scripts/deploy-s2i.sh" >&2
    exit 1
  fi
}

start_binary_build() {
  echo ""
  echo "==> Upload JAR and start S2I build"
  oc start-build "${APP_NAME}" \
    --from-file="${JAR_FILE}" \
    --follow
}

deploy_app() {
  echo ""
  echo "==> Deploy application from ImageStream"
  if app_exists; then
    echo "App resources already present — ImageStream update should trigger rollout"
    # Best-effort nudge for Deployment that tracks the IS
    if oc get deployment "${APP_NAME}" >/dev/null 2>&1; then
      oc rollout status "deployment/${APP_NAME}" --timeout=180s || true
    fi
    return
  fi
  oc new-app "${APP_NAME}"
}

ensure_route() {
  [[ "${CREATE_ROUTE}" == "true" ]] || return 0
  echo ""
  echo "==> Ensure Route"
  if oc get route "${APP_NAME}" >/dev/null 2>&1; then
    echo "Route already exists"
    return
  fi

  # NAME is positional: `oc create route edge NAME --service=SERVICE`
  # (there is no --name flag on this command in current oc)
  if oc create route edge "${APP_NAME}" --service="${APP_NAME}"; then
    return
  fi

  echo "edge route create failed — falling back to: oc expose service/${APP_NAME}" >&2
  oc expose "service/${APP_NAME}"
}

print_url() {
  local host
  host="$(oc get route "${APP_NAME}" -o jsonpath='{.spec.host}' 2>/dev/null || true)"
  if [[ -z "${host}" ]]; then
    echo "Route not found for ${APP_NAME}" >&2
    return 1
  fi
  echo "https://${host}"
}

show_status() {
  echo ""
  echo "==> Status"
  oc get buildconfig,build,imagestream,deployment,svc,route -l "app=${APP_NAME}" 2>/dev/null || true
  echo ""
  oc get buildconfig "${APP_NAME}" 2>/dev/null || true
  oc get builds -l "buildconfig=${APP_NAME}" 2>/dev/null || oc get builds | grep "${APP_NAME}" || true
  oc get pods -l "deployment=${APP_NAME}" 2>/dev/null \
    || oc get pods | grep "${APP_NAME}" || true
  echo ""
  local url
  if url="$(print_url 2>/dev/null)"; then
    echo "Application URL : ${url}"
    echo "Swagger UI      : ${url}/swagger-ui/index.html"
    if [[ "${INSECURE_CURL}" == "true" ]]; then
      echo "Smoke test      : curl -k -I ${url}/swagger-ui/index.html"
    else
      echo "Smoke test      : curl -I ${url}/swagger-ui/index.html"
    fi
  fi
}

ensure_logged_in

case "${MODE}" in
  status)
    show_status
    ;;
  url)
    print_url
    ;;
  rebuild)
    package_jar
    if ! buildconfig_exists; then
      echo "BuildConfig missing — run without --rebuild first." >&2
      exit 1
    fi
    start_binary_build
    show_status
    ;;
  deploy)
    package_jar
    create_s2i_build
    start_binary_build
    deploy_app
    ensure_route
    show_status
    ;;
esac

echo ""
echo "Done."
