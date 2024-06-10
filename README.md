# Test 관련 강의 학습

---   
'Practical Testing: 실용적인 테스트 가이드(박우빈, [링크](https://www.inflearn.com/course/practical-testing-%EC%8B%A4%EC%9A%A9%EC%A0%81%EC%9D%B8-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EA%B0%80%EC%9D%B4%EB%93%9C/))'  
'Java/Spring 테스트를 추가하고 싶은 개발자들의 오답노트(김우근, [링크](https://www.inflearn.com/course/%EC%9E%90%EB%B0%94-%EC%8A%A4%ED%94%84%EB%A7%81-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EA%B0%9C%EB%B0%9C%EC%9E%90-%EC%98%A4%EB%8B%B5%EB%85%B8%ED%8A%B8/))'  
두 강의를 수강하며 기록한 것들을 정리  
소스 코드는 'Practical Testing: 실용적인 테스트 가이드'의 것이지만, README에는 두 강의 내용 혼합해 기록   

---
## 테스트의 종류  

단위 테스트, 통합 테스트, 인수 테스트 크게 3가지 종류를 알고 있었는데, 두 강의에서 다루는 방식이 조금 달랐다.

|                  단위 테스트                   |                                                       소형 테스트                                                       |
|:-----------------------------------------:|:------------------------------------------------------------------------------------------------------------------:|
| 작은 코드 단위(Class or Method)를 독립적으로 검증하는 테스트 | 단일 서버, 단일 프로세스, 단일 스레드<br> 디스크 I/O 사용 X<br> Blocking call 허용X(Thread.sleep이 있다면 소형 테스트 X)<br> 결과가 결정적이고 테스트 속도가 빠름 |  

|                     통합 테스트                      |     중형 테스트      |
|:-----------------------------------------------:|:---------------:|
|         여러 모듈이 협력하는 기능을 통합적으로 검증하는 테스트          |      단일 서버      |
| 작은 범위의 단위 테스트만으로는, 기능 전체의 신뢰성을 보장할 수 없다고 판단해 수행 | 멀티 프로세스, 멀티 스레드 |
|              큰 기능 단위, 시나리오 단위를 검증               | h2와 같은 DB 사용 가능 |  

---
## 테스트 케이스의 세분화  
요구 사항을 곧이곧대로 믿지 말자  
추후에 무엇이 추가될지 모르며, 아직 확정되지 않은 사안이 있어 전달이 덜 되었거나, 숨겨진 것이 있을 수 있기 때문  

요구 사항에 범위 조건이 있다면 경계값 테스트를 고려해 보자.  
- 정보처리기사를 공부할 때 봤던 개념. 그냥 그런가 보다 하고 넘어갔었는데, 역시 괜히 배우는 게 아니다.  


### 테스트하기 어려운 영역을 구분하고 분리하자. 
테스트하기 어려운 영역
- 관측할 때마다 달라지는 것
  - 현재 날짜, 시간, 랜덤 값, 사용자 입력 등
- 외부 세계에 영향을 주는 코드
  - 표준 출력, 메시지 발송, DB 기록 등  

이런 것들은 외부로 분리하자.  
외부로 분리하는 방법에는 의존성 주입이 있다.  
하지만 끝도 없이 계속해서 상위 레벨로 넘겨버릴 수는 없을 것이다.  
그러니 의존성 주입뿐만 아니라, 의존성 역전을 더해 해결해 보려 하자.  

e.g.  
```java
import java.time.Clock;

public class User {
  // ...
  private Long lastLoginAt;
  // ...
  
  void login() {
    // ...
    this.lastLoginAt = Clock.systemUTC().millis();
  }
}
```  
유저가 로그인하면 로그인 시간을 기록한다고 해보자.  
이 상태로, 로그인 시간이 정확하게 기록됐는지 확인하는 테스트를 작성한다고 생각해 보자.  
벌써 머리가 아프다.  

의존성 주입을 통해 머리가 아픈 이유 중 일부는 해결할 수 있을 것이다.  
하지만 시간을 비교해야 하므로, 문제가 완벽하게 해결되지 않았다.  
```java
import java.time.Clock;

public class User {
  // ...
  private Long lastLoginAt;
  // ...
  
  void login(Clock clock) {
    // ...
    this.lastLoginAt = clock.millis();
  }
}
```  

의존성 역전을 사용해 보자.  
```java
public interface ClockHolder {
    long millis();
}

@Component
public class SystemClockHolder implements ClockHolder {
  @Override
  public long millis() {
    return Clock.systemUTC().millis();
  }
}

@RequiredArgsConstructor
public class TestClockHolder implements ClockHolder {
  private final long millis;

  @Override
  public long millis() {
    return millis;
  }
}
```  
실제 프로덕션, 개발 코드에는 SystemClockHolder를 사용하고, 테스트할 때는 TestClockHolder를 사용해 문제를 해결할 수 있게 된다.  

---
## TDD(Test Driven Development)  
테스트 코드를 먼저 작성하고, 테스트가 구현 과정을 주도하도록 한다.  

RED(실패하는 테스트 작성)  
GREEN(테스트 통과하기 위한 최소한의 구현)  
BLUE(or Refactor, 테스트가 통과하도록 유지하며 구현 코드를 개선)  

RED > GREEN > BLUE 순으로 진행하고, 다시 RED로 돌아가며 이를 계속해서 반복해 나가는 것.  

---
## 테스트는 문서다.  
DisplayName을 섬세하게 적자.  

~~`음료 1개 추가 테스트`~~ > ~~`음료를 1개 추가할 수 있다.`~~ > `음료를 1개 추가하면 주문 목록에 담긴다.`  

코드, 기능을 모르는 사람이 보더라도 이해할 수 있도록 섬세하게 적자.  
단어로 끝내지 않고, 명사의 나열보다는 문장으로 적자.  
테스트 행위에 대한 결과까지 기술하자.  

코드는 Given, When, Then을 통해 환경, 행동, 상태 변화를 나타내자.  

---  
## Test Double  
Dummy
- 아무것도 하지 않는 깡통 객체  
- 그저 코드가 정상적으로 돌아가게 하도록 만들기 위해 전달하는 객체  

Fake  
- 단순한 형태로 동일한 기능은 수행하나, 프로덕션에서 쓰기는 부족한 객체  

Stub  
- 테스트에서 요청한 것에 대해 미리 준비한 결과를 제공하는 객체  
- 그 외에는 응답하지 않는다.  

Spy  
- Stub이면서 호출된 내용을 기록하여 보여줄 수 있는 객체  
- 일부는 실제 객체처럼 동작하도록 하고, 일부만 Stubbing 할 수 있다.  

Mock  
- 행위에 대한 기대를 명세하고, 그에 따라 동작하도록 만들어진 객체  

---  
## Test Fixture  
테스트를 위해 원하는 상태로 고정한 일련의 객체  
테스트에 필요한 자원을 미리 생성하는 것  

테스트 별로 given 절의 데이터들이 겹치는 경우가 자주 발생하는데,  
그렇다고 @BeforeAll, @BeforeEach를 이용해 미리 만들어 버리면, 테스트 간 결합도가 생긴다.  

용어를 듣고 Builder를 이용해 최소한의 필드만 채운 객체를 테스트 픽스처로 생성하는 건 어떤지 생각해 봤는데,   
변경 사항이 있어 객체의 필드 수가 줄어들거나, 늘어나면 이를 반영해야 하기 때문에 이것도 좋은 방법은 아닌 것 같다는 생각을 했다.  

강의에서도 이와 비슷한 고려 사항을 언급한다.
@BeforeEach를 이용한 setUp 메소드는 언제 사용해야 하는가?
1. 각 테스트 입장에서 봤을 때, 아예 몰라도 테스트 내용을 이해하는 데 문제가 없는가  
2. 수정해도 모든 테스트에 영향을 주지 않는가  

두 가지를 만족하면 사용해도 될 것 같다 라고 언급하는데, 그 만큼 신중하게 생각해서 적용해야 할 것 같다.  
정말 몰라도 되는 내용이더라도, 테스트의 수가 많아지면 setUp 메소드를 확인하기 위해 위 아래로 스크롤을 움직여야 할 가능성이 있으니,  
가독성 측면도 고려해야 할 것 같다.  

---
## Business Layer Test(Service Test)  
Persistence Layer와의 상호작용을 통해 비즈니스 로직을 수행  
Business Layer만 테스트하는 것이 아닌, Persistence Layer도 통합해 테스트  
강의에서 @SpringBootTest와 @DataJpaTest의 차이를 언급한다.  

설정 면에서 차이도 있지만, 두 어노테이션의 가장 큰 차이는 @Transactional인 것 같다.  
@DataJpaTest에는 @Transactional이 붙어있지만, @SpringBootTest에는 없다.   
@Transactional이 붙어있는 경우 테스트마다 자동으로 롤백 되어 편리하지만, 명확한 트랜잭션 경계가 설정되어야 동작하는 JPA의 더티 체킹과 같은 기능 때문에 혼동이 생길 수도 있다.  
이러한 점을 잘 고려해서 테스트 클래스에 @Transactional, @DataJpaTest를 사용하자.  

이 외 차이점  
@SpringBootTest의 경우 모든 Bean을 등록해 우리가 SpringBoot 애플리케이션을 실행했을 때와 동일하게 스프링이 뜬다.  
@DataJpaTest의 경우 JPA에 필요한 설정들만 등록하고, in-memory DB(e.g. H2)를 사용한다.  
JPA에 필요한 설정들만 등록하고 관련 Bean들만 주입하기 때문에, 테스트 코드에 @Service나 @Controller와 같이 component로 등록한 Bean은 등록되지 않기 때문에, 주입받을 수 없다.  

---  
## Presentation Layer Test(Controller Test)  
외부 세계의 요청을 가장 먼저 받는 계층  
파라미터에 대한 최소한의 검증을 수행  

### Mock  
Mock 객체를 활용해 네트워크를 타는 기능, 외부 서비스, 불필요한 기능같이 실제로 수행하지 않았으면 하는 기능들을 수행하지 않도록 할 수 있음  
테스트하는 기능 내부에 있는 하위 기능을 Mock 객체로 대체하는 경우, Stubbing으로 볼 수 있다.  

### MockMvc  
Mock 객체를 사용해 스프링 MVC 동작을 재현할 수 있는 테스트 프레임워크  
테스트 클래스에 @WebMvcTest를 붙인 후 사용하자.  

@WebMvcTest의 경우 controller를 테스트하기 위한 어노테이션으로 Web과 관련된 의존성만을 가져온다.  
@Controller, @ControllerAdvice, @JsonComponent, Converter, GenericConverter, Filter, HandlerInterceptor,  
WebMvcConfigurer, WebMvcRegistrations, HandlerMethodArgumentResolver  
위 Bean들만 스캔해 @Component가 달려있던 것들은 등록되지 않는다.  

그렇기 때문에 controller에서 사용하는 service를 가져올 수 잆기 때문에, 이를 Mocking(@MockBean)해 사용한다.  


### Validation(검증)에 대한 책임 분리  
도메인 정책이 있을 때, '이것을 Request 객체의 필드에 달아서 검증하는 것이 맞는가?'를 고민해 보자.  
즉, '이게 controller 단에서 쳐낼 책임인가?'를 고민해 보자는 것.  
controller 단에서는 필드 자료형에 따라 최소한의 검증만 하고, 실제 도메인 정책에 따른 검증은 service 단에서 수행할 수도 있다는 것이다.  

---  
## 테스트를 위한 조언  
하나의 테스트에 하나의 주제만을 넣자.  
- 테스트 코드에 반복문, 분기분과 같은 논리 구조가 들어가면 두 가지 이상의 주제가 들어가는 것으로 볼 수 있다.  
- 또한 이러한 논리 구조는 테스트 코드를 읽는 사람이 고민, 생각을 하게 만든다.  

@ParameterizedTest  
- 동일한 로직을 값이나 환경, 조건을 바꾸어 여러 번 반복하고 싶을 때 사용해 보자.  

@DynamicTest  
- 일련의 시나리오를 테스트할 때 사용해 보자.  

테스트 수행도 비용이다. 환경을 통합하자.  
- 공통으로 사용되는 어노테이션을 떼어내 이를 몰아넣은 추상 객체를 만든 후 상속받도록 할 수도 있다.  

private 메소드의 테스트  
- 할 필요도 없고, 하려고 해서도 안 된다.  
- private 메소드가 테스트하고 싶어진다면, 객체를 분리할 시점인가?를 고민해야할 시점이다.  
- 이전에 프로젝트를 하면서 테스트 코드를 작성해 커밋하지는 않았지만, 로컬에서 잠시 테스트할 때 했던 고민이었다.