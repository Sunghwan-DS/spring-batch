# Spring Batch Study

## 1. 스프링 배치 소개

## 2. 스프링 배치 시작
### [2.1. 스프링 배치가 작동하기 위한 어노테이션 선언](https://github.com/Sunghwan-DS/spring-batch/commit/6f4924c20f174cef9a4938017beeb8f3826686b8)

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

### [2.2. Hello Spring Batch Job 구성하기](https://github.com/Sunghwan-DS/spring-batch/commit/033d97ca74667d36004b09c8be1c1b75d659a607)
Job이 구동되면 Step을 실행하고 Step이 구동되면 Taskelt을 실행하도록 설정함.

Job은 처리될 전체 일을 의미하여 Step은 일의 각 항목, Taskelt은 Step에서 이루어질 실제 비지니스 로직을 담게 된다.


### 2.3. DB 스키마 생성

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


### [2.4. table이 누락되었을 때 배치 동작 테스트용 Configuration 추가](https://github.com/Sunghwan-DS/spring-batch/commit/7e5feecf27a157cbcb2b06a646d78273f21e757f)

테이블이 누락된 경우
- mysql initialize-schema: never 인 경우에는 Table doesn't exist 로 SQLSyntaxErrorException 오류 발생.
- h2 메모리 DB의 경우 default 설정인 embedded 로 오류없이 정상 실행된다.


### 2.5. DB 스키마
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


## 3. 스프링 배치 도메인 이해
### 3.1. Job
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

### 3.2. JobInstance
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

### 3.3. JobParameter
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

### 3.4. JobExecution
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

### 3.5. Step
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

### 3.6. StepExecution
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

### 3.7. StepContribution
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

### 3.8. ExecutionContext
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

### 3.9. JobRepository
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

### 3.10. JobLauncher
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

## 4. 스프링 배치 실행 - Job
### 4.1. 배치 초기화 설정
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

### 4.2. JobBuilderFactory / JobBuilder
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

### 4.3. SimpleJob
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

### 4.4. SimpleJob - validator()
1. 기본 개념
   - Job 실행에 꼭 필요한 파라미터를 검증하는 용도
   - DefaultJobParametersValidator 구현체를 지원하며, 좀 더 복잡한 제약 조건이 있다면 인터페이스를 직접 구현할 수도 있음
2. 구조
   - JobParametersValidator - void validate(@Nullable JobParameters parameters)
   - JobParameters 값을 매개변수로 받아 검증함

### 4.5. SimpleJob - preventRestart()
1. 기본 개념
   - Job 의 재시작 여부를 설정
   - 기본 값은 true 이며 false 로 설정 시 "이 Job 은 재시작을 지원하지 않는다" 라는 의미
   - Job 이 실패해도 재시작이 안되며 Job 을 재시작하려고 하면 JobRestartException 이 발생
   - 재시작과 관련있는 기능으로 Job 을 처음 실행하는 것과는 아무런 상관 없음

### 4.6. SimpleJob - incrementer()
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

## 5. 스프링 배치 실행 - Step
### 5.1. StepBuilderFactory / StepBuilder
1. StepBuilderFactory
   - StepBuilder 를 생성하는 팩토리 클래스로서 get(String name) 메서드 제공
   - StepBuilderFactory.get("stepName")
     - "stepName" 으로 Step 을 생성
2. StepBuilder
   - Step 을 구성하는 설정 조건에 따라 다섯 개의 하위 빌더 클래스를 생성하고 실제 Step 생성을 위임한다
   - TaskletStepBuilder
     - TaskletStep 을 생성하는 기본 빌더 클래스
   - SimpleStepBuilder
     - TaskletStep 을 생성하며 내부적으로 청크기반의 작업을 처리하는 ChunkOrientedTasklet 클래스를 생성한다
   - PartitionStepBuilder
     - PartitionStep 을 생성하며 멀티 스레드 방식으로 Job 을 실행한다
   - JobStepBuilder
     - JobStep 을 생성하여 Step 안에서 Job 을 실행한다
   - FlowStepBuilder
     - FlowStep 을 생성하여 Step 안에서 Flow 를 실행한다

### 5.2. TaskletStep
1. 기본 개념
   - 스프링 배치에서 제공하는 Step 의 구현체로서 Tasklet 을 실행시키는 도메인 객체
   - RepeatTemplate 를 사용해서 Tasklet 의 구문을 트랜잭션 경계 내에서 반복해서 실행함
   - Task 기반과 Chunk 기반으로 나누어서 Tasklet 을 실행함
2. Task vs Chunk 기반 비교
   - 스프링 배치에서 Step 의 실행 단위는 크게 2가지로 나누어짐
     - chunk 기반
       - 하나의 큰 덩어리를 n개씩 나눠서 실행한다는 의미로 대량 처리를 하는 경우 효과적으로 설계 됨
       - ItemReader, ItemProcessor, ItemWriter 를 사용하며 청크 기반 전용 Tasklet 인 ChunkOrientedTasklet 구현체가 제공된다
     - Task 기반
       - ItemReader 와 ItemWriter 와 같은 청크 기반의 작업보다 단일 작업 기반으로 처리되는 것이 더 효율적인 경우
       - 주로 Tasklet 구현체를 만들어 사용
       - 대량 처리를 하는 경우 chunk 기반에 비해 더 복잡한 구현 필요

```java
public Step batchStep() {
    return stepBuilderFactory.get("batchStep")  // StepBuilder 를 생성하는 팩토리, Step 의 이름을 매개변수로 받음
    .tasklet(Tasklet)                           // Tasklet 클래스 설정, 이 메서드를 실행하면 TaskletStepBuilder 반환
    .startLimit(10)                             // Step 의 실행 횟수를 설정, 설정한 만큼 실행되고 초과시 오류 발생, 기본값은 INTEGER.MAX_VALUE
    .allowStartIfComplete(true)                 // Step 의 성공, 실패와 상관없이 항상 Step 을 실행하기 위한 설정
    .listener(StepExecutionListener)            // Step 라이프 사이클의 특정 시점에 콜백 제공받도록 StepExecutionListener 설정
    .build();                                   // TaskletStep 을 생성
}
```

## 6. 스프링 배치 실행 - Flow
### 6.12. @JobScope / @StepScope - 기본개념 및 설정
- Scope
  - 스프링 컨테이너에서 빈이 관리되는 범위
  - singleton, prototype, request, session, application 있으며 기본은 singleton 으로 생성됨
- 스프링 배치 스코프
  - @JobScope, @StepScope
    - Job 과 Step 의 빈 생성과 실행에 관여하는 스코프
    - 프록시 모드를 기본값으로 하는 스코프 - @Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
      - @Values 를 주입해서 빈의 실행 시점에 값을 참조할 수 있으며 일종의 Lazy Binding 이 가능해진다
      - @Value("#{jobParameters[파라미터명]}"), @Value("#{jobExecutionContext[파라미터명]"}), @Value("#{stepExecutionContext[파라미터명]"})
      - @Values 를 사용할 경우 빈 선언문에 @JobScope , @StepScope 를 정의하지 않으면 오류를 발생하므로 반드시 선언해야 함
    - 프록시 모드로 빈이 선언되기 때문에 어플리케이션 구동시점에는 빈의 프록시 객체가 생성되어 실행 시점에 실제 빈을 호출해 준다
    - 병렬처리 시 각 스레드 마다 생성된 스코프 빈이 할당되기 때문에 스레드에 안전하게 실행이 가능하다
- @JobScope
  - Step 선언문에 정의한다
  - @Value : jobParameter, jobExecutionContext 만 사용가능
- @StepScope
  - Tasklet 이나 ItemReader, ItemWriter, ItemProcessor 선언문에 정의한다
  - @Value : jobParameter, jobExecutionContext, stepExecutionContext 사용가능

### 6.13. @JobScope / @StepScope - 아키텍처
- Proxy 객체 생성
  - @JobScope , @StepScope 어노테이션이 붙은 빈 선언은 내부적으로 빈의 Proxy 객체가 생성된다 
    - @JobScope 
      - @Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
    - @StepScope 
      - @Scope(value = “step", proxyMode = ScopedProxyMode.TARGET_CLASS*
  - Job 실행 시 Proxy 객체가 실제 빈을 호출해서 해당 메서드를 실행시키는 구조
- JobScope, StepScope 
  - Proxy 객체의 실제 대상이 되는 Bean 을 등록, 해제하는 역할 
  - 실제 빈을 저장하고 있는 JobContext, StepContext 를 가지고 있다 
- JobContext, StepContext 
  - 스프링 컨테이너에서 생성된 빈을 저장하는 컨텍스트 역할 
  - Job 의 실행 시점에서 프록시 객체가 실제 빈을 참조할 때 사용됨

## 7. 스프링 배치 청크 프로세스 이해
### 7.1. Chunk
1. 기본 개념
   - Chunk 란 여러 개의 아이템을 묶은 하나의 덩어리, 블록을 의미
   - 한 번에 하나씩 아이템을 입력 받아 Chunk 단위의 덩어리로 만든 후 Chunk 단위로 트랜잭션을 처리함, 즉 Chunk 단위의 Commit 과 Rollback 이 이루어짐
   - 일반적으로 대용량 데이터를 한 번에 처리하는 것이 아닌 청크 단위로 쪼개어서 더 이상 처리할 데이터가 없을 때까지 반복해서 입출력하는데 사용됨
   - Chunk<I> vs Chunk<O>
     - Chunk<I> 는 ItemReader 로 읽은 하나의 아이템을 Chunk 에서 정한 개수만큼 반복해서 저장하는 타입
     - Chunk<O> 는 ItemReader 로부터 전달받은 Chunk<I> 를 참조해서 ItemProcessor 에서 적절하게 가공, 필터링한 다음 ItemWriter 에 전달하는 타입

### 7.2. ChunkOrientedTasklet
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
     
### 7.3. ChunkProvider
1. 기본 개념
   - ItemReader 를 사용해서 소스로부터 아이템을 Chunk size 만큼 읽어서 Chunk 단위로 만들어 제공하는 도메인 객체
   - Chunk<I> 를 만들고 내부적으로 반복문을 사용해서 ItemReader.read() 를 계속 호출하면서 item 을 Chunk 에 쌓는다
   - 외부로부터 ChunkProvider 가 호출될 때마다 항상 새로운 Chunk 가 생성된다
   - 반복문 종료 시점
     - Chunk size 만큼 item 을 읽으면 반복문 종료되고 ChunkProcessor 로 넘어감
     - ItemReader 가 읽은 item 이 null 일 경우 반복문 종료 및 해당 Step 반복문까지 종료
   - 기본 구현체로서 SimpleChunkProvider 와 FaultTolerantChunkProvider 가 있다

### 7.4. ChunkProcessor
1. 기본 개념
   - ItemProcessor 를 사용해서 Item 을 변형, 가공, 필터링하고 ItemWriter 를 사용해서 Chunk 데이터를 저장, 출력한다
   - Chunk<O> 를 만들고 앞에서 넘어온 Chunk<I> 의 item 을 한 건씩 처리한 후 Chunk<O> 에 저장한다
   - 외부로부터 ChunkProcessor 가 호출될 때마다 항상 새로운 Chunk 가 생성된다
   - ItemProcessor 는 설정 시 선택사항으로서 만약 객체가 존재하지 않을 경우 ItemReader 에서 읽은 item 그대로가 Chunk<O> 에 저장된다
   - ItemProcessor 처리가 완료되면 Chunk<O> 에 있는 List<Item> 을 ItemWriter 에게 전달한다
   - ItemWriter 처리가 완료되면 Chunk 트랜잭션이 종료하게 되고 Step 반복문에서 ChunkOrientedTasklet 가 새롭게 실행된다
   - ItemWriter 는 Chunk size 만큼 데이터를 Commit 처리하기 때문에 Chunk size 는 곧 Commit Interval 이 된다
   - 기본 구현체로서 SimpleChunkProcessor 와 FaultTolerantChunkProcessor 가 있다

### 7.5. ItemReader
1. 기본 개념
   - 다양한 입력으로부터 데이터를 읽어서 제공하는 인터페이스
     - 플랫(Flat) 파일 - csv, txt (고정 위치로 정의된 데이터 필드나 특수문자로 구별된 데이터의 행)
     - XML, Json
     - Database
     - JMS, RabbitMQ 와 같은 Message Queuing 서비스
     - Custom Reader - 구현 시 멀티 스레드 환경에서 스레드에 안전하게 구현할 필요가 있음
   - ChunkOrientedTasklet 실행 시 필수적 요소로 설정해야 한다
2. 구조
   - ItemReader<T>
     - T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException;
   - T read()
     - 입력 데이터를 읽고 다음 데이터로 이동한다
     - 아이템 하나를 리턴하여 더 이상 아이템이 없는 경우 null 리턴
     - 아이템 하나는 파일의 한 줄, DB 의 한 row 혹은 XML 파일에서 하나의 엘리먼트가 될 수 있다
     - 더 이상 처리해야 할 Item 이 없어도 예외가 발생하지 않고 ItemProcessor 와 같은 다음 단계로 넘어 간다
   - 다수의 구현체들이 ItemReader 와 ItemStream 인터페이스를 동시에 구현하고 있음
     - 파일의 스트림을 열거나 종료, DB 커넥션을 열거나 종료, 입력 장치 초기화 등의 작업
     - ExecutionContext 에 read 와 관련된 여러 가지 상태 정보를 저장해서 재시작 시 다시 참조하도록 지원
   - 일부를 제외하고 하위 클래스들은 기본적으로 스레드에 안전하지 않기 때문에 병렬 처리 시 데이터 정합성을 위한 동기화 처리 필요

### 7.6. ItemWriter
1. 기본 개념
   - Chunk 단위로 데이터를 받아 일괄 출력 작업을 위한 인터페이스
     - 플랫(Flat) 파일 - csv, txt
     - XML, Json
     - Database
     - JMS, RabbitMQ 와 같은 Message Queuing 서비스
     - Mail Service
     - Custom Writer
   - 아이템 하나가 아닌 아이템 리스트를 전달 받는다
   - ChunkOrientedTasklet 실행 시 필수적 요소로 설정해야 한다
2. 구조
   - ItemWriter<T>
     - void write(List<? extends T> items) throws Exception
   - void write(List<? extents T> items)
     - 출력 데이터를 아이템 리스트로 받아 처리한다
     - 출력이 완료되고 트랜잭션이 종료되면 새로운 Chunk 단위 프로세스로 이동한다
   - 다수의 구현체들이 ItemWriter 와 ItemStream 을 동시에 구현하고 있다
     - 파일의 스트림을 열거나 종료, DB 커넥션을 열거나 종료, 출력 장치 초기화 등의 작업
   - 보통 ItemReader 구현체와 1:1 대응 관계인 구현체들로 구성되어 있다

### 7.7. ItemProcessor
1. 기본 개념
   - 데이터를 출력하기 전에 데이터를 가공, 변형, 필터링하는 역할
   - ItemReader 및 ItemWriter 와 분리되어 비즈니스 로직을 구현할 수 있다
   - ItemReader 로 부터 받은 아이템을 특정 타입으로 변환해서 ItemWriter 에 넘겨줄 수 있다
   - ItemReader 로 부터 받은 아이템들 중 필터 과정을 거쳐 원하는 아이템들만 ItemWriter 에게 넘겨줄 수 있다
       - ItemProcessor 에서 process() 실행결과 null 을 반환하면 Chunk<O> 에 저장되지 않기 때문에 결국 ItemWriter 에 전달되지 않는다
   - ChunkOrientedTasklet 실행 시 선택적 요소이기 때문에 청크 기반 프로세싱에서 ItemProcessor 단계가 반드시 필요한 것은 아니다
2. 구조
   - ItemProcessor<I, O>
     - O process(@NonNull I item) throws Exception
   - O process
       - <I> 제네릭은 ItemReader 에서 받은 데이터 타입 지정
       - <O> 제네릭은 ItemWriter 에게 보낼 데이터 타입 지정
       - 아이템 하나씩 가공 처리하며 null 리턴할 경우 해당 아이템은 Chunk<O> 에 저장되지 않음
   - ItemStream 을 구현하지 않는다
   - 거의 대부분 Customizing 해서 사용하기 때문에 기본적으로 제공되는 구현체가 적다

### 7.8 ItemStream
1. 기본 개념
   -  ItemReader 와 ItemWriter 처리 과정 중 상태를 저장하고 오류가 발생하면 해당 상태를 참조하여 실패한 곳에서 재시작 하도록 지원
   - 리소스를 열고(open) 닫아야(close) 하며 입출력 장치 초기화 등의 작업을 해야하는 경우
   - ExecutionContext 를 매개변수로 받아서 상태 정보를 업데이트(update) 한다
   - ItemReader 및 ItemWriter 는 ItemStream 을 구현해야 한다
2. 구조
   - ItemStream
     - ```java
       // read, write 메서드 호출 전에 파일이나 커넥션이 필요한 리소스에 접근하도록 초기화 작업
       void open(ExecutionContext executionContext) throws ItemStreamException
       
       // 현재까지 진행된 모든 상태를 저장
       void update(ExecutionContext executionContext) throws ItemStreamException
       
       // 열려있는 모든 리소스를 안전하게 해제하고 닫음
       void close() throws ItemStreamException
       ```

## 8. 스프링 배치 청크 프로세스 활용 - ItemReader
### 8.1. XML StaxEventItemReader
1. 개념 및 API 소개
   - JAVA XML API
     - DOM 방식
       - 문서 전체를 메모리에 로드한 후 Tree 형태로 만들어서 데이터를 처리하는 방식, pull 방식
       - 엘리멘트 제어는 유연하나 문서 크기가 클 경우 메모리 사용이 많고 속도가 느림
     - SAX 방식
       - 문서의 항목을 읽을 때 마다 이벤트가 발생하여 데이터를 처리하는 push 방식
       - 메모리 비용이 적고 속도가 빠른 장점은 있으나 엘리멘트 제어가 어려움
     - StAX 방식 (Streaming API for XML)
       - DOM 과 SAX 의 장점과 단점을 보완한 API 모델로서 push 와 pull 을 동시에 제공함
       - XML 문서를 읽고 쓸 수 있는 양방향 파서기 지원
       - XML 파일의 항목에서 항목으로 직접 이동하면서 Stax 파서기를 통해 구문 분석
       - 유형
         - Iterator API 방식
           - XMLEventReader 의 nextEvent() 를 호출해서 이벤트 객체를 가지고 옴
           - 이벤트 객체는 XML 태그 유형 (요소, 텍스트, 주석 등) 에 대한 정보를 제공함
         - Cursor API 방식
           - JDBC ResultSet 처럼 작동하는 API 로서 XMLStreamReader 는 XML 문서의 다음 요소로 커서를 이동한다
           - 커서에서 직접 메서드를 호출하여 현재 이벤트에 대한 자세한 정보를 얻는다
   - Spring-OXM
     - 스프링의 Object XML Mapping 기술로 XML 바인딩 기술을 추상화함
       - Marshaller
         - marshall - 객체를 XML 로 직렬화하는 행위
       - Unmarchaller
         - unmarshall - XML 을 객체로 역직렬화하는 행위
       - Marshaller 와 Unmarshaller 바인딩 기능을 제공하는 오픈소스로 JaxB2, Castor, XmlBeans, Xstream 등이 있다
     - 스프링 배치는 특정한 XML 바인딩 기술을 강요하지 않고 Spring OXM 에 위임한다
       - 바인딩 기술을 제공하는 구현체를 선택해서 처리하도록 한다
   - Spring Batch XML
     - 스프링 배치에서는 StAX 방식으로 XML 문서를 처리하는 StaxEventItemReader 를 제공한다
     - XML 을 읽어 자바 객체로 매핑하고 자바 객체를 XML 로 쓸 수 있는 트랜잭션 구조를 지원
2. StAX 아키텍처
   - XML 전체 문서가 아닌 조각 단위로 구문을 분석하여 처리할 수 있다.
     - 루트 엘리먼트 사이에 있는 것들은 전부 하나의 조각(Fragment) 을 구성한다
   - 조각을 읽을 때 DOM 의 pull 방식을 사용하고 조각을 객체로 바인딩 처리하는 것은 SAX 의 push 방식을 사용한다
   - ```java
     public StaxEventItemReader itemReader() {
        return StaxEventItemReaderBuilder<T>().name(String name)
                                              .resource(Resource)                               // 읽어야 할 리소스 설정
                                              .addFlagmentRootElements(String... rootElements)  // Fragment 단위의 루트 엘리먼트 설정, 이 루트 조각 단위가 객체와 매핑하는 기준
                                              .unmarshaller(Unmarshaller)                       // Unmarshaller 객체 설정
                                              .saveState(boolean)                               // 상태 정보 저장 여부 설정, 기본값은 true
                                              .build();                      
     }
     ```
     
3. StaxEventItemReader 기본 개념
   - Stax API 방식으로 데이터를 읽어들이는 ItemReader
   - Spring-OXM 과 Xstream 의존성을 추가해야 한다
   - pom.xml
     - ```xml
       <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-oxm</artifactId>
         <version>5.3.7</version>
       </dependency>
       <dependency>
         <groupId>com.thoughworks.xstream</groupId>
         <artifactId>xstream</artifactId>
         <version>1.4.16</version>
       </dependency>
       ```
   - StaxEventItemReader<T>
     - ```java
       // XML 조각을 독립형으로 XML 문서로 처리하는 것을 지원하는 이벤트 판독기
       FragmentEventReader fragmentReader
       
       // XML 이벤트 구문 분석을 위한 최상위 인터페이스
       XMLEventReader eventReader
       
       // XML 문서를 객체로 직렬화하는 인터페이스
       Unmarshaller unmarshaller
       
       // 다양한 리소스에 접근하도록 추상화한 인터페이스
       Resource resource
       
       // 조각단위의 루트 엘리먼트명을 담은 리스트 변수
       List<QName> fragmentRootElementNames
       ```

### 8.2. DB - Cursor & Paging 이해
- 기본 개념
  - 배치 어플리케이션은 실시간적 처리가 어려운 대용량 데이터를 다루며 이 때 DB I/O 의 성능문제와 메모리 자원의 효율성 문제를 해결할 수 있어야 한다
  - 스프링 배치에서는 대용량 데이터 처리를 위한 두 가지 해결방안을 제시하고 있다
- Cursor Based 처리
  - 현재 행에 커서를JDBC ResultSet 의 기본 메커니즘을 사용
  - 유지하며 다음 데이터를 호출하면 다음 행으로 커서를 이동하며 데이터 반환이 이루어지는 Streaming 방식의 I/O 이다
  - ResultSet이 open 될 때마다 next() 메소드가 호출 되어 Database의 데이터가 반환되고 객체와 매핑이 이루어진다.
  - DB Connection 이 연결되면 배치 처리가 완료될 때 까지 데이터를 읽어오기 때문에 DB와 SocketTimeout을 충분히 큰 값으로 설정 필요
  - 모든 결과를 메모리에 할당하기 때문에 메모리 사용량이 많아지는 단점이 있다
  - Connection 연결 유지 시간과 메모리 공간이 충분하다면 대량의 데이터 처리에 적합할 수 있다 (fetchSize 조절)
- Paging Based 처리
  - 페이징 단위로 데이터를 조회하는 방식으로 Page Size 만큼 한번에 메모리로 가지고 온 다음 한 개씩 읽는다.
  - 한 페이지를 읽을때마다 Connection을 맺고 끊기 때문에 대량의 데이터를 처리하더라도 SocketTimeout 예외가 거의 일어나지 않는다
  - 시작 행 번호를 지정하고 페이지에 반환시키고자 하는 행의 수를 지정한 후 사용 – Offset, Limit
  - 페이징 단위의 결과만 메모리에 할당하기 때문에 메모리 사용량이 적어지는 장점이 있다
  - Connection 연결 유지 시간이 길지 않고 메모리 공간을 효율적으로 사용해야 하는 데이터 처리에 적합할 수 있다

### 8.3. DB - JdbcCursorItemReader
- 기본 개념
  - Spring Batch 4.3 버전부터 지원함
  - Cursor 기반의 JPA 구현체로서 EntityManagerFactory 객체가 필요하며 쿼리는 JPQL 을 사용한다
- API
```java
public JdbcCursorItemReader itemReader() {
    return new JdbcCursorItemReaderBuilder<T>()
    .name("cursorItemReader")
    .fetchSize(int chunkSize)       // Cursor 방식으로 데이터를 가지고 올 때 한번에 메모리에 할당할 크기를 설정한다
    .dataSource(DataSource)         // DB 에 접근하기 위해 Datasource 설정
    .rowMapper(RowMapper)           // 쿼리 결과로 반환되는 데이터와 객체를 매핑하기 위한 RowMapper 설정
    .beanRowMapper(Class<T>)        // 별도의 RowMapper 을 설정하지 않고 클래스 타입을 설정하면 자동으로 객체와 매핑
    .sql(String sql)                // ItemReader 가 조회할 때 사용할 쿼리 문장 설정
    .queryArguments(Object... args) // 쿼리 파라미터 설정
    .maxItemCount(int count)        // 조회할 최대 item 수
    .currentItemCount(int count)    // 조회 item 의 시작 지점
    .maxRows(int maxRows)           // ResultSet 오브젝트가 포함할 수 있는 최대 행 수
    .build();
}
```

### 8.4. DB - JpaCursorItemReader
- 기본 개념
  - Spring Batch 4.3 버전부터 지원
  - Cursor 기반의 JPA 구현체로서 EntityManagerFactory 객체가 필요하며 쿼리는 JPQL 을 사용한다
- API
```java
public JpaCursorItemReader itemReader() {
    return new JpaCursorItemReaderBuilder<T>()
    .name("cursorItemReader")
    .queryString(String JPQL)                       // ItemReader 가 조회할 때 사용할 JPQL 문장 설정
    .EntityManagerFactory(EntityManagerFactory)     // JPQL 을 실행하는 EntityManager 를 생성하는 팩토리
    .parameterValue(Map<String, Object> parameters) // 쿼리 파라미터 설정
    .maxItemCount(int count)                        // 조회할 최대 item 수
    .currentItemCount(int count)                    // 조회 Item 의 시작 지점
    .build();
}
```

### 8.5. DB - JdbcPagingItemReader
- 기본 개념
  - Paging 기반의 JDBC 구현체로서 쿼리에 시작 행 번호 (offset) 와 페이지에서 반환할 행 수 (limit)를 지정해서 SQL 을 실행한다
  - 스프링 배치에서 offset과 limit을 PageSize에 맞게 자동으로 생성해 주며 페이징 단위로 데이터를 조회할 때 마다 새로운 쿼리가 실행한다
  - 페이지마다 새로운 쿼리를 실행하기 때문에 페이징 시 결과 데이터의 순서가 보장될 수 있도록 order by 구문이 작성되도록 한다
  - 멀티 스레드 환경에서 Thread 안정성을 보장하기 때문에 별도의 동기화를 할 필요가 없다
- PagingQueryProvider
  - 쿼리 실행에 필요한 쿼리문을 ItemReader 에게 제공하는 클래스
  - 데이터베이스마다 페이징 전략이 다르기 때문에 각 데이터 베이스 유형마다 다른 PagingQueryProvider 를 사용한다
  - Select 절, from 절, sortKey 는 필수로 설정해야 하며 where, group by 절은 필수가 아니다
```java
public JdbcPagingItemReader itemReader() {
    return new JdbcPagingItemReaderBuilder<T>()
    .name("pagingItemReader")
    .pageSize(int pageSize)                             // 페이지 크기 설정 (쿼리 당 요청할 레코드 수)
    .dataSource(DataSource)                             // DB 에 접근하기 위해 Datasource 설정
    .queryProvider(PagingQueryProvicer)                 // DB 페이징 전략에 따른 PagingQueryProvider 설정
    .rowMapper(Class<T>)                                // 쿼리 결과로 반환되는 데이터와 객체를 매핑하기 위한 RowMapper 설정
    .selectClause(String selectClause)                  // select 절 설정
    .fromClause(String fromClause)                      // from 절 설정
    .whereClause(String whereClause)                    // where 절 설정
    .groupClause(String groupClause)                    // group 절 설정
    .sortKeys(Map<String, Order> sortKeys)              // 정렬을 위한 유니크한 키 설정
    .parameterValues(Map<String, Object> parameters)    // 쿼리 파라미터 설정
    .maxItemCount(int count)                            // 조회할 최대 item 수
    .currentItemCount(int count)                        // 조회 Item 의 시작 지점
    .maxRows(int maxRows)                               // ResultSet 오브젝트가 포함할 수 있는 최대 행 수
    .build();
}
```

### 8.6. DB - JpaPagingItemReader
- 기본 개념
  - Paging 기반의 JPA 구현체로서 EntityManagerFactory 객체가 필요하며 쿼리는 JPQL 을 사용한다
- API
```java
public JpaPagingItemReader itemReader() {
    return new JpaPagingItemReaderBuilder<T>()
    .name("pagingItemReader")
    .pageSize(int count)                            // 페이지 크기 설정 (쿼리 당 요청할 레코드 수)
    .queryString(String JPQL)                       // ItemReader 가 조회할 때 사용할 JPQL 문장 설정
    .EntityManagerFactory(EntityManagerFactory)     // JPQL 을 실행하는 EntityManager 를 생성하는 팩토리
    .parameterValue(Map<String, Object> parameters) // 쿼리 파라미터 설정
    .build();
}
```

## 9. 스프링 배치 청크 프로세스 활용 - ItemWriter
### 9.1. DB - JdbcBatchItemWriter
- 기본 개념
  - JdbcCursorItemReader 설정과 마찬가지로 datasource 를 지정하고, sql 속성에 실행할 쿼리를 설정
  - JDBC 의 Batch 기능을 사용하여 bulk insert/update/delete 방식으로 처리
  - 단건 처리가 아닌 일괄처리이기 때문에 성능에 이점을 가진다
- API
```java
public JdbcBatchItemWriter itemWriter() {
    return new JdbcBatchItemWriterBuilder<T>()
    .name(String name)
    .datasource(Datasource) // DB 에 접근하기 위해 Datasource 설정
    .sql(String sql)        // ItemWriter 가 사용할 쿼리 문장 설정
    .assertUpdates(boolean) // 트랜잭션 이후 적어도 하나의 항목이 행을 업데이트 혹은 삭제하지 않을 경우 예외발생여부를 설정함, 기본값은 true
    .beanMapped()           // Pojo 기반으로 Insert SQL 의 Values 를 매핑
    .columnMapped()         // Key, Value 기반으로 Insert SQL 의 Values 를 매핑
    .build();
}
```

### 9.2. DB - JpaItemWriter
- 기본 개념
  - JPA Entity 기반으로 데이터를 처리하며 EntityManagerFactory 를 주입받아 사용한다
  - Entity 를 하나씩 chunk 크기 만큼 insert  혹은 merge 한 다음 flush 한다
  - ItemReader 나 ItemProcessor 로 부터 아이템을 전발 받을 때는 Entity 클래스 타입으로 받아야 한다
- API
```java
public JpaItemWriter itemWriter() {
    return new JpaItemWriterBuilder<T>()
    .usePersist(boolean)                        // Entity 를 persist() 할 것인지 여부 설정, false 이면 merge() 처리
    .entityManagerFactory(EntityManagerFactory) // EntityManagerFactory 설정
    .build();
}
```


## 10. 스프링 배치 청크 프로세스 활용 - ItemProcessor
### 10.1. CompositeItemProcessor
1. 기본 개념
   - ItemProcessor 들은 연결(Chaining)해서 위임하면 각 ItemProcessor 를 실행시킨다
   - 이전 ItemProcessor 반환 값은 다음 ItemProcessor 값으로 연결된다
2. API
```java
public ItemProcessor itemProcessor() {
    return new CompositeItemProcessorBuilder<>()
    .delegate(ItemProcessor<?, ?>... delegates) // 체이닝 할 ItemProcessor 객체 설정
    .build();
    }
```

### 10.2. ClassifierCompositeItemProcessor
1. 기본 개념
   - Classifier 로 라우팅 패턴을 구현해서 ItemProcessor 구현체 중에서 하나를 호출하는 역할을 한다
2. API
```java
public ItemProcessor itemProcessor() {
    return new ClassifierCompositeItemProcessorBuilder<>()
    .classifier(Classifier) // 분류자 설정
    .build();
    }
```
Classifier<C, T> // C의 분류에 따라 적절한 T 를 반환
T classify

## 11. 스프링 배치 반복 및 오류 제어
### 11.1. Repeat
- 기본개념
  - Spring Batch 는 얼마나 작업을 반복해야 하는지 알려줄 수 있는 기능을 제공한다
  - 특정 조건이 충족 될 때까지 (또는 특정 조건이 아직 충족되지 않을 때까지) Job 또는 Step 을 반복하도록 배치 애플리케이션을 구성 할 수 있다
  - 스프링 배치에서는 Step 의 반복과 Chunk 반복을 RepeatOperation 을 사용해서 처리하고 있다
  - 기본 구현체로 RepeatTemplate 를 제공한다
- 반복을 종료할 것인지 여부를 결정하는 세가지 항목
  - RepeatStatus
    - 스프링 배치의 처리가 끝났는지 판별하기 위한 열거형(enum)
      - CONTINUABLE - 작업이 남아 있음
      - FINISHED - 더 이상의 반복 없음
  - CompletionPolicy
    - RepeatTemplate 의 iterate 메소드 안에서 반복을 중단할지 결정
    - 실행횟수또는완료시기,오류발생시수행할작업에대한반복여부결정
    - 정상 종료를 알리는데 사용된다
  - ExceptionHandler
    - RepeatCallback 안에서 예외가 발생하면 RepeatTemplate 가 ExceptionHandler 를 참조해서 예외를 다시 던질지 여부 결정
    - 예외를 받아서 다시 던지게 되면 반복 종료
    - 비정상 종료를 알리는데 사용된다

### 11.2. FaultTolerant
- 기본개념
  - 스프링 배치는 Job 실행 중에 오류가 발생할 경우 장애를 처리하기 위한 기능을 제공하며 이를 통해 복원력을 향상시킬 수 있다
  - 오류가 발생해도 Step 이 즉시 종료되지 않고 Retry 혹은 Skip 기능을 활성화 함으로써 내결함성 서비스가 가능하도록 한다
  - 프로그램의 내결함성을 위해 Skip 과 Retry 기능을 제공한다
    - Skip
      - ItemReader / ItemProcessor / ItemWriter 에 적용 할 수 있다
    - Retry
      - ItemProcessor / ItemWriter 에 적용할 수 있다
  - FaultTolerant 구조는 청크 기반의 프로세스 기반위에 Skip 과 Retry 기능이 추가되어 재정의 되어 있다

```java
public Step batchStep() {
    return new stepBuilderFactory.get("batchStep")
    .<I, O>chunk(10)
    .reader(ItemReader)
    .writer(ItemWriter)
    .faultTolerant()                                // 내결함성 기능 활성화
    .skip(Class<? extends Throwable> type)          // 예외 발생 시 Skip 할 예외 타입 설정
    .skipLimit(int skipLimit)                       // Skip 제한 횟수 설정
    .skipPolicy(SkipPolicy skipPolicy)              // Skip 을 어떤 조건과 기준으로 적용 할 것인지 정책 설정
    .noSkip(Class<? extends Throwable> type)        // 예외 발생 시 Skip 하지 않을 예외 타입 설정
    .retry(Class<? extends Throwable> type)         // 예외 발생 시 Retry 할 예외 타입 설정
    .retryLimit(int retryLimit)                     // Retry 제한 횟수 설정
    .retryPolicy(RetryPolicy retryPolicy)           // Retry 를 어떤 조건과 기준으로 적용 할 것인지 정책 설정
    .backOffPolicy(BackOffPolicy backOffPolicy)     // 다시 Retry 하기 까지의 지연시간 (단위:ms)을 설정
    .noRetry(Class<? extends Throwable> type)       // 예외 발생 시 Retry 하지 않을 예외 타입 설정
    .noRollback(Class<? extends Throwable> type)    // 예외 발생 시 Rollback 하지 않을 예외 타입 설정
    .build();
```

## 12. 스프링 배치 멀티 스레드 프로세싱

## 13. 스프링 배치 이벤트 리스너
### 13.1. 기본 개념
- 기본 개념
    - Listener 는 배치 흐름 중에 Job, Step, Chunk 단계의 실행 전후에 발생하는 이벤트를 받아 용도에 맞게 활용할 수 있도록 제공하는 인터셉터 개념의 클래스
    - 각 단계별로 로그기록을 남기거나 소요된 시간을 계산하거나 실행상태 정보들을 참조 및 조회할 수 있다
    - 이벤트를 받기 위해서는 Listener 를 등록해야 하며 등록은 API 설정에서 각 단계별로 지정할 수 있다
- Listeners
    - Job
        - JobExecutionListener - Job 실행 전후
    - Step
        - StepExecutionListener - Step 실행 전후
        - ChunkListener - Chunk 실행 전후 (Tasklet 실행 전후), 오류 시점
        - ItemReadListener - ItemReader 실행 전후, 오류 시점, item 이 null 일 경우 호출 안됨
        - ItemProcessListener - ItemProcessor 실행 전후, 오류 시점, item 이 null 일 경우 호출 안됨
        - ItemWriteListener - ItemWriter 실행 전후, 오류 시점, item 이 null 일 경우 호출 안됨
    - SkipListener - 읽기, 쓰기, 처리 Skip 실행 시점, Item 처리가 Skip 될 경우 Skip 된 item 을 추적함
    - RetryListener - Retry 시작, 종료, 에러 시점
- 구현 방법
    - 어노테이션 방식
        - @BeforeStep
        - @AfterStep 등
        - 인터페이스를 구현할 필요가 없다
        - 클래스 및 메서드명을 자유롭게 작성할 수 있다
        - Object 타입의 listener 로 설정하기 위해서는 어노테이션 방식으로 구현해야 한다
    - 인터페이스 방식
        - implements StepExecutionListener

## 14. 스프링 배치 테스트 및 운영
### 15.1. Spring Batch Test
- 스프링 배치 4.1.x 이상 버전 (부트 2.1) 기준
- pom.xml
  - ```xml
    <dependency>
        <groupId>org.springframework.batch</groupId>
        <artifactId>spring-batch-test</artifactId>
    </dependency>
    ```
- @SpringBatchTest
  - 자동으로 ApplicationContext 에 테스트에 필요한 여러 유틸 Bean 을 등록해주는 어노테이션
    - JobLauncherTestUtils
      - launchJob(), launchStep() 과 같은 스프링 배치 테스트에 필요한 유틸성 메소드 지원
    - JobRepositoryTestUtils
      - JobRepository 를 사용해서 JobExecution 을 생성 및 삭제 기능 메소드 지원
    - StepScopeTestExecutionListener
      - @StepScope 컨텍스트를 생성해주며 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있다
    - JobScopeTestExecutionListener
      - @JobScope 컨텍스트를 생성해주며 해당 컨텍스트를 통해 JobParameter 등을 단위 테스트에서 DI 받을 수 있다
- JobLauncherTestUtils
  - ```java
    // 실행할 Job 을 자동으로 주입 받음
    // 한 개의 Job 만 받을 수 있음 (Job 설정클래스를 한 개만 지정해야 함)
    @Autowired
    void setJob(Job job)
    
    // Job 을 실행시키고 JobExecution 을 반환
    JobExecution launchJob(JobParameters jobParameters)
    
    // Step 을 실행시키고 JobExecution 을 반환
    JobExecution launchStep(String stepName)
    ```
- JobRepositoryTestUtils
  - ```java
    // JobExecution 생성 - job 이름, step 이름, 생성 개수
    List<JobExecution> createJobExecutions(String jobName, String[] stepNames, int Count)
    
    // JobExecution 삭제 - JobExecution 목록
    void removeJobExecutions(Collection<JobExecution> list)
    ```
- ```java
  @RunWith(SpringRunner.class)
  @SpringBatchTest
  @SpringBootTest(classes={BatchJobConfiguration.class, TestBatchConfig.class})
  public class BatchJobConfigurationTest {
    ...
  }
  ```
  - @SpringBatchTest - JobLauncherTestUtils, JobRepositoryTestUtils 등을 제공하는 어노테이션
  - @SpringBootTest(classes={...}) - Job 설정 클래스 지정, 통합 테스트를 위한 여러 의존성 빈들을 주입 받기 위한 어노테이션
- ```java
  @Configuration
  @EnableAutoConfiguration
  @EnableBatchProcessing
  public class TestBatchConfig {}
  ```
  - @EnableBatchProcessing - 테스트 시 배치환경 및 설정 초기화를 자동 구동하기 위한 어노테이션
  - 테스트 클래스마다 선언하지 않고 공통으로 사용하기 위함
    
### 15.2. JobExplorer / JobRegistry / JobOperator
- JobExplorer
  - JobRepository 의 readonly 버전
  - 실행 중인 Job 의 실행 정보인 JobExecution 또는 Step 의 실행 벙보인 StepExecution 을 조회할 수 있다
- JobRegistry
  - 생성된 Job 을 자동으로 등록, 추적 및 관리하며 여러 곳에서 job 을 생성한 경우 ApplicationContext 에서 job 을 수집해서 사용할 수 있다
  - 기본 구현체로 map 기반의 MapJobRegistry 클래스를 제공한다
    - jobName 을 Key 로 하고 job 을 값으로 하여 매핑한다
  - Job 등록
    - JobRegistryBeanPostProcessor - BeanPostProcessor 단계에서 bean 초기화 시 자동으로 JobRegistry 에 Job 을 등록 시켜준다
- JobOperator
  - JobExplorer, JobRepositorym, JobRegistry, JobLauncher 를 포함하고 있으며 배치의 중단, 재시작, job 요약 등의 모니터링이 가능하다
  - 기본 구현체로 SimpleJobOperator 클래스를 제공한다
