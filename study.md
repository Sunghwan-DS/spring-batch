# Spring Batch Study

### [1. 스프링 배치가 작동하기 위한 어노테이션 선언](https://github.com/Sunghwan-DS/spring-batch/commit/6f4924c20f174cef9a4938017beeb8f3826686b8)

@EnableBatchProcessing -> SimpleBatchConfiguration -> BatchConfigurerConfiguration(BasicBatchController, JpaBatchConfigurer)

1. SimpleBatchConfiguration
   - 스프링 배치가 초기화 될 때 자동으로 실행되는 설정 클래스
   - Job을 수행하는 JobLauncherApplicationRunner 빈을 생성
2. SimpleBatchConfiguration
   - JobBuilderFactory 와 StepBuilderFactory 생성
   - 스프링 배치의 주요 구성 요소 생성 - 프록시 객체로 생성됨
3. BatchConfigurerConfiguration
   - BasicBatchController
     - SimpleBatchConfiguration 에서 생성된 프록시 객체의 실제 대상 객체를 생성하는 설정 클래스
     - 빈으로 의존성 주입 받아서 주요 객체들을 참조해서 사용할 수 있다
   - JpaBatchConfigurer
     - JPA 관련 객체를 생성하는 설정 클래스
   - 사용자 정의 BatchConfigurer 인터페이스를 구현하여 사용할 수 있음


### [2. Hello Spring Batch Job 구성하기](https://github.com/Sunghwan-DS/spring-batch/commit/033d97ca74667d36004b09c8be1c1b75d659a607)

Job이 구동되면 Step을 실행하고 Step이 구동되면 Taskelt을 실행하도록 설정함.

Job은 처리될 전체 일을 의미하여 Step은 일의 각 항목, Taskelt은 Step에서 이루어질 실제 비지니스 로직을 담게 된다.


### 3. DB 스키마 생성

1. docker 설치
2. $ docker pull mysql
3. $ docker run --name springboot-mysql -e MYSQL_ROOT_PASSWORD=pass -d -p 3306:3306 mysql
4. $ docker start springboot-mysql
5. $ docker exec -it springboot-mysql bash
6. $ mysql -u root -p

(+) window 환경에서 docker 사용하기
1. 관리자 권한으로 파워쉘 열기
2. 리눅스 서브시스템 활성 명령어 입력 (dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart)
3. 가상 머신 플랫폼 기능 활성화 명령어 입력 (dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart)
4. x64 머신용 최신 WSL2 Linux 커널 업데이트 패키지 다운로드 및 설치

##### DB 생성
$ CREATE DATABASE springbatch default CHARACTER SET UTF8

##### 테이블 생성
spring-batch-core-4.3.8.jar > org > springframework > batch > core > schema-mysql.sql 참조


### [4. table이 누락되었을 때 배치 동작 테스트용 Configuration 추가](https://github.com/Sunghwan-DS/spring-batch/commit/7e5feecf27a157cbcb2b06a646d78273f21e757f)

테이블이 누락된 경우
- mysql initialize-schema: never 인 경우에는 Table doesn't exist 로 SQLSyntaxErrorException 오류 발생.
- h2 메모리 DB의 경우 default 설정인 embedded 로 오류없이 정상 실행된다.


### 5. DB 스키마
- Job 관련 테이블
  - BATCH_JOB_INSTANCE
    - Job 이 실행될 때 JobInstance 정보가 저장되며 job_name과 job_key를 키로 하여 하나의 데이터가 저장
    - 동일한 job_name과 job_key로 중복 저장될 수 없다
  - BATCH_JOB_EXECUTION
    - Job의 실행정보가 저장되며 JOB 생성, 시작, 종료 시간, 실행상태, 메시지 등을 관리
  - BATCH_JOB_EXECUTION_PARAMS
    - Job과 함께 실행되는 JobParameter 정보를 저장
  - BATCH_JOB_EXECUTION_CONTEXT
    - Job의 실행동안 여러가지 상태정보, 공유 데이터를 직렬화(Json 형식)해서 저장
    - Step 간 서로 공유 가능함
- Step 관련 테이블
  - BATCH_STEP_EXECUTION
    - Step의 실행정보가 저장되며 생성, 시작, 종료 시간, 실행상태, 메시지 등을 관리
  - BATCH_STEP_EXECUTION_CONTEXT
    - Step의 실행동안 여러가지 상태정보, 공유 데이터를 직렬화(Json 형식)해서 저장
    - Step 별로 저장되며 Step 간 서로 공유할 수 없음
