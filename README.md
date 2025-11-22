# 텔레그램 RabbitMQ 배치 서비스

Quarkus 기반의 경량 배치 서비스로, 텔레그램을 통해 배치 작업을 관리하고 RabbitMQ로 메시지를 전송합니다.

## 기능
- **텔레그램 봇 관리**: 텔레그램 채팅을 통해 배치를 등록, 삭제, 조회, 실행할 수 있습니다.
- **RabbitMQ 연동**: 스케줄링된 작업이나 수동 명령을 통해 RabbitMQ로 메시지를 발행합니다.
- **동적 스케줄링**: 크론 표현식을 사용하여 런타임에 작업을 예약합니다.
- **네이티브 빌드**: GraalVM을 사용하여 경량 네이티브 이미지로 빌드됩니다.

## 시작하기

### 필수 조건
- JDK 21+
- Docker (RabbitMQ 실행용)
- Telegram Bot Token

### 로컬 실행
1. RabbitMQ 실행:
   ```bash
   docker run -d --hostname my-rabbit --name some-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```
2. `application.properties` 설정:
   `src/main/resources/application.properties`에서 `telegram-bot-token`을 설정하거나 환경 변수로 주입하세요.
3. 앱 실행:
   ```bash
   ./mvnw quarkus:dev
   ```

### 텔레그램 명령어
- `/new {이름} {크론} {메시지}`: 새로운 배치 등록
  - 예: `/new myjob "0/10 * * * * ?" Hello World` (10초마다 "Hello World" 전송)
- `/remove {이름}`: 배치 삭제
- `/list {개수}`: 등록된 배치 목록 조회
- `/exec {이름}`: 배치 즉시 실행
- `/send {메시지}`: RabbitMQ로 메시지 즉시 전송

## 배포
GitHub Actions를 통해 main 브랜치 푸시 시 자동으로 네이티브 이미지를 빌드하고 Docker Hub에 푸시합니다.

### Docker 실행
```bash
docker run -d \
  -p 8080:8080 \
  -e AMQP_HOST=host.docker.internal \
  -e TELEGRAM_BOT_TOKEN=your_token \
  -v $(pwd)/batch-data:/work/data \
  your-docker-user/batch-service:latest
```
> **참고**: `-v $(pwd)/batch-data:/work/data` 옵션을 사용하면 배치 등록 정보(`batch-jobs.json`)가 호스트의 `batch-data` 폴더에 저장되어, 컨테이너를 재시작해도 데이터가 유지됩니다.

### 설정 관리 (Configuration)
Docker 환경에서 `application.properties`를 관리하는 방법은 두 가지가 있습니다.

**1. 환경 변수 사용 (권장)**
Quarkus는 환경 변수를 자동으로 속성에 매핑합니다. 가장 간편한 방법입니다.
- `amqp-host` -> `AMQP_HOST`
- `telegram-bot-token` -> `TELEGRAM_BOT_TOKEN`
- `quarkus.http.port` -> `QUARKUS_HTTP_PORT`

**2. 설정 파일 마운트**
호스트의 `application.properties` 파일을 컨테이너 내부로 마운트하여 덮어쓸 수 있습니다.
```bash
docker run -d \
  -v $(pwd)/config/application.properties:/work/config/application.properties \
  ...
```
Quarkus는 실행 위치의 `config/application.properties`를 우선적으로 읽습니다.

### 헬스 체크
- Liveness: `http://localhost:8080/q/health/live`
- Readiness: `http://localhost:8080/q/health/ready`
