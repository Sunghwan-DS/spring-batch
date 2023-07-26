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

### 6.9. JobRepository
1. 기본 개념
    - 배치 작업 중의 정보를 저장하는 저장소 역할
    - Job 이 언제 수행되었고, 언제 끝났으며, 몇 번이 실행되었고 실행에 대한 결과 등의 배치 작업의 수행과 관련된 모든 meta data 를 저장함
      - JobLauncher, Job, Step 구현체 내부에서 CRUD 기능을 처리함
2. 설정
   - @EnableBatchProcessing 어노테이션만 선언하면 JobRepository 가 자동으로 빈으로 생성됨
   - BatchConfigurer 인터페이스를 구현하거나 BasicBatchConfigurer 를 상속해서 JobRepository 설정을 커스터마이징 할 수 있다.
     - JDBC 방식으로 설정 - JobRepositoryFactoryBean
       - 내부적으로 AOP 기술을 통해 트랜잭션 처리를 해주고 있음
       - 트랜잭션 isolation 의 기본값은 SERIALIZABLE 로 최고 수준, 다른 레벨(READ_COMMITED, REPEATABLE_READ)로 지정 가능
       - 메타테이블의 Table Prefix 를 변경할 수 있음, 기본 값은 "BATCH_" 임
       - ```java
         @Override
         protected JobRepository createJobRepository() throws Exception {
            JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
            factory.setDataSource(dataSource);
            factory.setTransactionManager(transationManager);
            factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE"); // isolation 수준, 기본값은 "ISOLATION_SERIALIZABLE"
            factory.setTablePrefix("SYSTEM_"); // 테이블 Prefix, 기본값은 "BATCH_JOB_EXECUTION 가 SYSTEM_JOB_EXECUTION 으로 변경됨
            factory.setMaxVarCharLength(1000); // varchar 최대 길이(기본값 2500)
            return factory.getObject(); // Proxy 객체가 생성됨 (트랜잭션 Advice 적용 등을 위해 AOP 기술 적용)
         }
         ```
     - In Memory 방식으로 설정 - MapJobRepositoryFactoryBean
       - 성능 등의 이유로 도메인 오브젝트를 굳이 데이터베이스에 저장하고 싶지 않은 경우
       - 보통 Test 나 프로토타입의 빠른 개발이 필요할 때 사용
       - ```java
         @Override
         protected JobRepository createJobRepository() throws Exception {
            MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
            factory.setTransactionManager(transactionManager); // ResourcelessTransactionManager 사용
            return factory.getObject();
         }
         ```

### 6.10. JobLauncher
1. 기본 개념
   - 배치 Job 을 실행시키는 역할을 한다
   - Job 과 Job Parameters 를 인자로 받으며 요청된 배치 작업을 수행한 후 최종 client 에게 JobExecution 을 반환함
   - 스프링 부트 배치가 구동이 되면 JobLauncher 빈이 자동 생성 된다
   - Job 실행
     - JobLauncher.run(Job, JobParameters)
     - 스프링 부트 배치에서는 JobLauncherApplicationRunner 가 자동적으로 JobLauncher 을 실행시킨다
     - 동기적 실행
       - taskExecutor 를 SyncTaskExecutor 로 설정할 경우 (기본값은 SyncTaskExecutor)
       - JobExecution 을 획득하고 배치 처리를 최종 완료한 이후 Client 에게 JobExecution 을 반환
       - 스케쥴러에 의한 배치처리에 적합 함 - 배치처리시간이 길어도 상관없는 경우
     - 비 동기적 실행
       - taskExecutor 가 SimpleAsyncTaskExecutor 로 설정할 경우
       - JobExecution 을 획득한 후 Client 에게 바로 JobExecution 을 반환하고 배치처리를 완료한다
       - HTTP 요청에 의한 배치처리에 적합함 - 배치처리 시간이 길 경우 응답이 늦어지지 않도록 함
2. 구조
   - JobLauncher - JobExecution run(Job, JobParameters)

## 7. 스프링 배치 실행
### 7.1. 배치 초기화 설정
1. JobLauncherApplicationRunner
   - Spring Batch 작업을 시작하는 APPlicationRunner 로서 BatchAutoConfiguration 에서 생성됨
   - 스프링 부트에서 제공하는 ApplicationRunner 의 구현체로 어플리케이션이 정상적으로 구동되자 마다 실행됨
   - 기본적으로 빈으로 등록된 모든 job 을 실행시킨다
2. BatchProperties
   - Spring Batch 의 환경 설정 클래스
   - Job 이름, 스키마 초기화 설정, 테이블 Prefix 등의 값을 설정할 수 있다
   - application.properties or application.yml 파일에 설정함
     - ```yaml
       batch:
         job:
           name: ${job.name:NONE}
         initialize-schema: NEVER
         tablePrefix: SYSTEM
       ```
3. Job 실행 옵션
   - 지정한 Batch Job 만 실행하도록 할 수 있음
   - spring.batch.job.name: ${job.name:NONE}
   - 어플리케이션 실행시 Program arguments 로 job 이름 입력한다
     - --job.name=helloJob
     - --job.name=helloJob,simpleJob (하나 이상의 job 을 실행 할 경우 쉼표로 구분해서 입력함)

### 7.2. JobBuilderFactory / JobBuilder
1. 스프링 배치는 Job 과 Step 을 쉽게 생성 및 설정할 수 있도록 util 성격의 빌더 클래스를 제공함
2. JobBuilderFactory
   - jobBuilder 를 생성하는 팩토리 클래스로서 get(String name) 메서드 제공
   - jobBuilderFactory.get("jobName")
     - "jobName" 은 스프링 배치가 Job 을 실행시킬 때 참조하는 JOB 의 이름
3. JobBuilder
   - Job 을 구성하는 설정 조건에 따라 두 개의 하위 빌더 클래스를 생성하고 실제 Job 생성을 위임한다
   - SimpleJobBuilder
     - SimpleJob 을 생성하는 Builder 클래스
     - Job 실행과 관련된 여러 설정 API 를 제공한다
   - FlowJobBuilder
     - FlowJob 을 생성하는 Builder 클래스
     - 내부적으로 FlowBuilder 를 반환함으로써 Flow 실행과 관련된 여러 설정 API 를 제공한다

### 7.3. SimpleJob
1. 기본 개념
   - SimpleJob 은 Step 을 실행시키는 Job 구현체로서 SimpleJobBuilder 에 의해 생성된다
   - 여러 단계의 Step 으로 구성할 수 있으며 Step 을 순차적으로 실행시킨다
   - 모든 Step 의 실행이 성공적으로 완료되어야 Job 이 성공적으로 완료된다
   - 맨 마지막에 실행한 Step 의 BatchStatus 가 Job 의 최종 BatchStatus 가 된다
2. 흐름
   - 순차적으로 실행되는 각 Step 의 응답값이 모두 COMPLETED 여야 Job 이 COMPLETED 의 응답값을 전달한다
   - 어떤 Step 이 실패하여 FAILED 의 응답값을 Job 에 전달할 경우 이후 Step 은 실행되지 않으며 Job 도 FAILED 의 응답값을 전달한다

#### 개념 및 API 소개
```java
public Job batchJob() {
    return jobBuilderFactory.get("batchJob")                        // JobBuilder 를 생성하는 팩토리, Job 의 이름을 매개변수로 받음
                            .start(Step)                            // 처음 실행 할 Step 설정, 최초 한번 설정, 이 메서드를 실행하면 SIMpleJobBuilder 반환
                            .next(Step)                             // 다음에 실행 할 Step 설정, 횟수는 제한이 없으며 모든 next() 의 Step 이 종료가 되면 Job 이 종료된다
                            .incrementer(JobParametersIncrementer)  // JobParameter 의 값을 자동으로 증가시켜주는 JobParametersIncremeter 설정
                            .preventRestart(true)                   // Job 의 재시작 가능 여부 설정, 기본값은 true
                            .validator(JobParameterValidator)       // JobParameter 를 실행하기 전에 올바른 구성이 되었는지 검증하는 JobParametersValidator 설정
                            .listener(JobExecutionListener)         // Job 라이프 사이클의 특정 시점에 콜백 제공받고록 JOBExecutionListener 설정
                            .build();                               // SimpleJob 생성
    }
```

### 7.4. SimpleJob - validator()
1. 기본 개념
   - Job 실행에 꼭 필요한 파라미터를 검증하는 용도
   - DefaultJobParametersValidator 구현체를 지원하며, 좀 더 복잡한 제약 조건이 있다면 인터페이스를 직접 구현할 수도 있음
2. 구조
   - JobParametersValidator - void validate(@Nullable JobParameters parameters)
   - JobParameters 값을 매개변수로 받아 검증함

### 7.5. SimpleJob - preventRestart()
1. 기본 개념
   - Job 의 재시작 여부를 설정
   - 기본 값은 true 이며 false 로 설정 시 "이 Job 은 재시작을 지원하지 않는다" 라는 의미
   - Job 이 실패해도 재시작이 안되며 Job 을 재시작하려고 하면 JobRestartException 이 발생
   - 재시작과 관련있는 기능으로 Job 을 처음 실행하는 것과는 아무런 상관 없음

### 7.6. SimpleJob - incrementer()
1. 기본 개념
   - JobParameters 에서 필요한 값을 증가시켜 다음에 사용될 JobParameters 오브젝트를 리턴
   - 기존의 JobParameter 변경없이 Job 을 여러 번 시작하고자 할 때
   - RunIdIncrementer 구현체를 지원하며 인터페이스를 직접 구현할 수 있음
   - ```java
     @Override
     public JobParameters getNext(@Nullable JobParameters parameters) {
        
        JobParameters params = (parameters == null) ? new JobParameters() : parameters;
        
        long id = params.getLong(key, new Long(0)) + 1;
        return new JobParametersBuilder(params).addLong(key, id).toJobParameters();
     }
     ```
2. 구조
   - JobParametersIncrementer - JobParameters getNext(@Nullable JobParameters parameters);

## 8. 스프링 배치 청크 프로세스 이해
### 8.1. Chunk
1. 기본 개념
   - Chunk 란 여러 개의 아이템을 묶은 하나의 덩어리, 블록을 의미
   - 한 번에 하나씩 아이템을 입력 받아 Chunk 단위의 덩어리로 만든 후 Chunk 단위로 트랜잭션을 처리함, 즉 Chunk 단위의 Commit 과 Rollback 이 이루어짐
   - 일반적으로 대용량 데이터를 한 번에 처리하는 것이 아닌 청크 단위로 쪼개어서 더 이상 처리할 데이터가 없을 때까지 반복해서 입출력하는데 사용됨
   - Chunk<I> vs Chunk<O>
     - Chunk<I> 는 ItemReader 로 읽은 하나의 아이템을 Chunk 에서 정한 개수만큼 반복해서 저장하는 타입
     - Chunk<O> 는 ItemReader 로부터 전달받은 Chunk<I> 를 참조해서 ItemProcessor 에서 적절하게 가공, 필터링한 다음 ItemWriter 에 전달하는 타입

### 8.2. ChunkOrientedTasklet
1. 기본 개념
   - ChunkOrientedTasklet 은 스프링 배치에서 제공하는 Tasklet 의 구현체로서 Chunk 지향 프로세싱을 담당하는 도메인 객체
   - ItemReader, ItemWriter, ItemProcessor 를 사용해 Chunk 기반의 데이터 입출력 처리를 담당한다
   - TaskletStep 에 의해서 반복적으로 실행되며 ChunkOrientedTasklet 이 실행될 때마다 매번 새로운 트랜잭션이 생성되어 처리가 이루어진다
   - exception 이 발생할 경우, 해당 Chunk 는 롤백되며 이전에 커밋한 Chunk 는 완료된 상태가 유지된다
   - 내부적으로 ItemReader 를 핸들링하는 ChunkProvider 와 ItemProcessor, ItemWriter 를 핸들링하는 ChunkProcessor 타입의 구현체를 가진다
   - ```java
     public Step chunkStep() {
        return stepBuilderFactory.get("chunkStep")
                                 .<I, O>chunk(10)               // chunk size 설정, chunk size 는 commit interval 을 의미함, input, output 제네릭타입 설정
                                 .<I, O>chunk(CompletionPolicy) // Chunk 프로세스를 완료하기 위한 정책 설정 클래스 지정
                                 .reader(ItemReader())          // 소스로부터 item 을 읽거나 가져오는 ItemReader 구현체 설정
                                 .writer(ItemWriter())          // item 을 목적지에 쓰거나 보내기 위한 ItemWriter 구현체 설정
                                 .processor(ItemProcessor())    // item 을 변형, 가공, 필터링하기 위한 ItemProcessor 구현체 설정
                                 .stream(ItemStream())          // 재시작 데이터를 관리하는 콜백에 대한 스트림 등록
                                 .readerlsTransactionQueue()    // Item 이 JMS, Message Queue Server 와 같은 트랜잭션 외부에서 읽혀지고 캐시할 것인지 여부, 기본값은 false
                                 .listener(ChunkListener)       // Chunk 프로세스가 진행되는 특정 시점에 콜백 제공받도록 ChunkListener 설정
                                 .build()
     }
     ```
     
### 8.3. ChunkProvider
1. 기본 개념
   - ItemReader 를 사용해서 소스로부터 아이템을 Chunk size 만큼 읽어서 Chunk 단위로 만들어 제공하는 도메인 객체
   - Chunk<I> 를 만들고 내부적으로 반복문을 사용해서 ItemReader.read() 를 계속 호출하면서 item 을 Chunk 에 쌓는다
   - 외부로부터 ChunkProvider 가 호출될 때마다 항상 새로운 Chunk 가 생성된다
   - 반복문 종료 시점
     - Chunk size 만큼 item 을 읽으면 반복문 종료되고 ChunkProcessor 로 넘어감
     - ItemReader 가 읽은 item 이 null 일 경우 반복문 종료 및 해당 Step 반복문까지 종료
   - 기본 구현체로서 SimpleChunkProvider 와 FaultTolerantChunkProvider 가 있다

### 8.4. ChunkProcessor
1. 기본 개념
   - ItemProcessor 를 사용해서 Item 을 변형, 가공, 필터링하고 ItemWriter 를 사용해서 Chunk 데이터를 저장, 출력한다
   - Chunk<O> 를 만들고 앞에서 넘어온 Chunk<I> 의 item 을 한 건씩 처리한 후 Chunk<O> 에 저장한다
   - 외부로부터 ChunkProcessor 가 호출될 때마다 항상 새로운 Chunk 가 생성된다
   - ItemProcessor 는 설정 시 선택사항으로서 만약 객체가 존재하지 않을 경우 ItemReader 에서 읽은 item 그대로가 Chunk<O> 에 저장된다
   - ItemProcessor 처리가 완료되면 Chunk<O> 에 있는 List<Item> 을 ItemWriter 에게 전달한다
   - ItemWriter 처리가 완료되면 Chunk 트랜잭션이 종료하게 되고 Step 반복문에서 ChunkOrientedTasklet 가 새롭게 실행된다
   - ItemWriter 는 Chunk size 만큼 데이터를 Commit 처리하기 때문에 Chunk size 는 곧 Commit Interval 이 된다
   - 기본 구현체로서 SimpleChunkProcessor 와 FaultTolerantChunkProcessor 가 있다

