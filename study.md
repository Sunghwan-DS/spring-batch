# Spring Batch Study

## [1. 스프링 배치가 작동하기 위한 어노테이션 선언](https://github.com/Sunghwan-DS/spring-batch/commit/6f4924c20f174cef9a4938017beeb8f3826686b8)

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


## [2. Hello Spring Batch Job 구성하기](https://github.com/Sunghwan-DS/spring-batch/commit/033d97ca74667d36004b09c8be1c1b75d659a607)

Job이 구동되면 Step을 실행하고 Step이 구동되면 Taskelt을 실행하도록 설정함.

Job은 처리될 전체 일을 의미하여 Step은 일의 각 항목, Taskelt은 Step에서 이루어질 실제 비지니스 로직을 담게 된다.


## 3. DB 스키마 생성

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

#### DB 생성
$ CREATE DATABASE springbatch default CHARACTER SET UTF8

#### 테이블 생성
spring-batch-core-4.3.8.jar > org > springframework > batch > core > schema-mysql.sql 참조


## [4. table이 누락되었을 때 배치 동작 테스트용 Configuration 추가](https://github.com/Sunghwan-DS/spring-batch/commit/7e5feecf27a157cbcb2b06a646d78273f21e757f)

테이블이 누락된 경우
- mysql initialize-schema: never 인 경우에는 Table doesn't exist 로 SQLSyntaxErrorException 오류 발생.
- h2 메모리 DB의 경우 default 설정인 embedded 로 오류없이 정상 실행된다.


## 5. DB 스키마
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


## 6. 스프링 배치 도메인 이해
### 6.1. Job
1. 기본 개념
   - 배치 계층 구조에서 가장 상위에 있는 개념으로서 하나의 배치작업 자체를 의미함
     - "API 서버의 접속 로그 데이터를 통계 서버로 옮기는 배치" 인 Job 자체를 의미한다
   - Job Configuration 을 통해 생성되는 개체 단위로서 배치작업을 어떻게 구성하고 실행할 것인지 전체적으로 설정하고 명세해 놓은 객체
   - 배치 Job을 구성하기 위한 최상위 인터페이스이며 스프링 배치가 기본 구현체를 제공한다
   - 여러 Step을 포함하고 있는 컨테이너로서 반드시 한 개 이상의 Step으로 구성해야 함
2. 기본 구현체
   - SimpleJob
     - 순차적으로 Step을 실행시키는 Job
     - 모든 Job에서 유용하게 사용할 수 있는 표준 기능을 갖고 있음
   - FlowJob
     - 특정한 조건과 흐름에 따라 Step을 구성하여 실행시키는 Job
     - Flow 객체를 실행시켜서 작업을 진행함

### 6.2. JobInstance
1. 기본 개념
   - Job 이 실행될 때 생성되는 Job 의 논리적 실행 단위 객체로서 고유하기 식별 가능한 작업 실행을 나타냄
   - Job 의 설정과 구성은 동일하지만 Job 이 실행되는 시점에 처리하는 내용은 다르기 때문에 JOB 의 실행을 구분해야 함
     - 예를 들어 하루에 한 번 씩 배치 Job 이 실행된다면 매일 실행되는 각각의 Job 을 JobInstance 로 표현합니다.
   - JobInstance 생성 및 실행
     - 처음 시작하는 Job + JobParameter 일 경우 새로운 JobInstance 생성
     - 이전과 동일한 Job + JobParameter 으로 실행할 경우 이미 존재하는 JobInstance 리턴
       - 내부적으로 JobName + jobKey (jobParametes 의 해시값) 를 가지고 JobInstance 객체를 얻음
     - Job 과는 1:M 관계
2. BATCH_JOB_INSTANCE 테이블과 매핑
   - JOB_NAME (Job) 과 JOB_KEY (JobParameter 해시값) 가 동일한 데이터는 중복해서 저장할 수 없음

### 6.3. JobParameter
1. 기본 개념
   - Job 을 실행할 때 함께 포함되어 사용되는 파라미터를 가진 도메인 객체
   - 하나의 Job 에 존재할 수 있는 여러 개의 JobInstance 를 구분하기 위한 용도
   - JobParameters 와 JobInstance 는 1:1 관계
2. 생성 및 바인딩
   - 어플리케이션 실행 시 주입
     - Java -jar LogBatch.jar requestDate=20230101
   - 코드로 생성
     - JobParameterBuilder, DefaultJobParametersConverter
   - SpEL 이용
     - @Value("#{jobParameter[requestDate]}"), @JobScope, @StepScope 선언 필수
3. BATCH_JOB_EXECUTION_PARAM 테이블과 매핑
   - JOB_EXECUTION 과 1:M 의 관계

### 6.4. JobExecution
1. 기본 개념
    - JobInstance 에 대한 한 번의 시도를 의미하는 개체로서 Job 실행 중에 발생한 정보들을 저장하고 있는 객체
      - 시작시간, 종료시간, 상태(시작됨,완료,실패), 종료상태의 속성을 가짐
    - JobInstance 와의 관계
      - JobExecution 은 'FAILED' 또는 'COMPLETED' 등의 Job 의 실행 결과 상태를 가지고 있음
      - JobExecution 의 실행 상태 결과가 'COMPLETED' 면 JobInstance 실행이 완료된 것으로 간주하고 재 실행이 불가함
      - JobExecution 의 실행 상태 결과가 'FAILED' 면 JobInstance 실행이 완료되지 않은 것으로 간주해서 재실행이 가능함
        - JobParameter 가 동일한 값으로 Job 을 실행할지라도 JobInstance 를 계속 실행할 수 있음
      - JobExecution 의 실행 상태 결과가 'COMPLETED' 될 때까지 하나의 JobInstance 내에서 여러 번의 시도가 생길 수 있음
2. BATCH_JOB_EXECUTION 테이블과 매핑
    - JobInstance 와 JobExecution 는 1:M 의 관계로서 JobInstance 에 대한 성공/실패의 내역을 가지고 있음

### 6.5. Step
1. 기본 개념
    - Batch job 을 구성하는 독깁적인 하나의 단계로서 실제 배치 처리를 정의하고 컨트롤하는 데 필요한 모든 정보를 가지고 있는 도메인 객체
    - 단순한 단일 태스크 뿐 아니라 입력과 처리 그리고 출력과 관련된 복잡한 비즈니스 로직을 포함하는 모든 설정들을 담고 있다.
    - 배치작업을 어떻게 구성하고 실행할 것인지 Job 의 세부 작업을 Task 기반으로 설정하고 명세해 놓은 객체
    - 모든 Job은 하나 이상의 step 으로 구성됨
2. 기본 구현체
    - TaskletStep
      - 가장 기본이 되는 클래스로서 Tasklet 타입의 구현체들을 제어한다
    - PartitionStep
      - 멀티 스레드 방식으로 Step 을 여러 개로 분리해서 실행한다
    - JobStep
      - Step 내에서 Job 을 실행하도록 한다
    - FlowStep
      - Step 내에서 Flow 를 실행하도록 한다

### 6.6. StepExecution
1. 기본 개념
    - Step 에 대한 한 번의 시도를 의미하는 객체로서 Step 실행 중에 발생한 정보들을 저장하고 있는 객체
      - 시작시간, 종료시간, 상태(시작됨, 완료, 실패), commit count, rollback count 등의 속성을 가짐
    - Step 이 매번 시도될 때마다 생성되며 각 Step 별로 생성된다
    - Job 이 재시작 하더라도 이미 성공적으로 완료된 각 Step 은 재실행되지 않고 실패한 Step 만 실행된다
    - 이전 단계 Step 이 실패해서 현재 Step 을 실행하지 않았다면 StepExecution 을 생성하지 않는다. Step 이 실제로 시작됐을 때만 StepExecution 을 생성한다
    - JobExecution 과의 관계
      - Step 의 StepExecution 이 모두 정상적으로 완료되어야 JobExecution 이 정상적으로 완료된다
      - Step 의 StepExecution 중 하나라도 실패하면 JobExecution 은 실패한다
2. BATCH_STEP_EXECUTION 테이블과 매핑
    - JobExecution 와 StepExecution 는 1:M 관계
    - 하나의 Job 에 여러 개의 Step 으로 구성했을 경우 각 StepExecution 은 하나의 JobExecution 을 부모로 가진다

### 6.7. StepContribution
1. 기본 개념
    - 청크 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트하는 도메인 객체
    - 청크 커밋 직전에 StepExecution 의 apply 메서드를 호출하여 상태를 업데이트 함
    - ExitStatus 의 기본 종료코드 외 사용자 정의 종료코드를 생성해서 적용할 수 있음
2. 구조
   - 성공적으로 read 한 아이템 수
   - 성공적으로 write 한 아이템 수
   - ItemProcessor 에 의해 필터링된 아이템 수
   - 부모 클래스인 StepExecution 의 총 skip 횟수
   - read 에 실패해서 스킵된 횟수
   - write 에 실패해서 스킵된 횟수
   - process 에 실패해서 스킵된 횟수
   - 실행결과를 나타내는 클래스로서 종료코드를 포함(UNKNOWN, EXECUTING, COMPLETED, NOOP, FAILED, STOPPED)
   - StepExecution 객체 저장

### 6.8. ExecutionContext
1. 기본 개념
   - 프레임워크에서 유지 및 관리하는 키/값으로 된 컬렉션으로 StepExecution 또는 JobExecution 객체의 상태(state)를 저장하는 공유 객체
   - DB 에 직렬화 한 값으로 저장됨 - {"key" : "value"}
   - 공유 범위
     - Step 범위 - 각 Step 의 StepExecution 에 저장되며 Step 간 서로 공유 안됨
     - Job 범위 - 각 Job 의 JobExecution 에 저장되며 Job 간 서로 공유 안되며 해당 Job 의 Step 간 서로 공유됨
   - Job 재 시작시 이미 처리된 Row 데이터는 건너뛰고 이후로 수행하도록 할 때 상태 정보를 활용한다
2. 구조
   - ExecutionContext - Map<String, Object> map = new ConcurrentHashMap
   - 유지, 관리에 필요한 키값 설정