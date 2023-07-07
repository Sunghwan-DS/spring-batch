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


### [3. DB 스키마 생성](https://github.com/Sunghwan-DS/spring-batch/commit/033d97ca74667d36004b09c8be1c1b75d659a607)

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
