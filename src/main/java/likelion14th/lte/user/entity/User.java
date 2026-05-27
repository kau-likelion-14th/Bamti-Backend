package likelion14th.lte.user.entity;

import jakarta.persistence.*;
import likelion14th.lte.Entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
// [Q1. @NoArgsConstructor는 매개변수가 없는 기본 생성자를 만듭니다.
// 그런데 왜 누구나 쓸 수 있게 PUBLIC으로 열어두지 않고, 굳이 PROTECTED로 막아두었을까요? (객체 생성의 안전성과 JPA 관점)]
// 답변: JPA는 DB에서 데이터 조회할 때 기본 생성자가 필요하다. 그러나 퍼블릭이면 외부에서 마음대로 생성될 수 있어 위험하다.
//      protected면 JPA는 사용 가능하지만 외부에서 생성하는 것을 막아 객체 생성을 Builder로만 제한할 수 ㅣㅇㅆ다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [Q2. @Column(nullable = false) 어노테이션이 DB와 자바 코드 사이에서 하는 역할은 무엇인가요?]
    // 답변: 1. 테이블 생성 시 해당 칼럼에 NOT NULL 조건으로 null 값 저장을 방지
    //      2. JPA가 엔티티 저장할 때 null 검증으로 DB로 가기 전에 예외 발생
    @Column(nullable = false)
    private String username;

    @Column(length = 16, nullable = false, unique = true)
    private String userTag;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Builder(access = AccessLevel.PUBLIC)
    private User (String username, String introduction, String userTag){
        this.username = username;
        this.userTag = userTag;
        this.introduction = introduction;
    }

    // [Q3. @Setter를 위 @Getter 처럼 사용하면 모든 맴버들에 setIntruduction() 같은 setter 메서드가 생성됩니다. 하지만 왜 @Setter를 쓰지않고 updateIntroduction() 이라는 명확한 메서드를 만든 객체지향적인 이유는 무엇인가요?]
    // 답변: @Setter 사용으로 모든 필드에 setter가 생성되어 외부에서 수정이 가능해진다.
    //      setInrroduction을 사용하면 객체에서 변경 가능한 필드가 introduction임을 명시하고
    //      나중에 introduction 수정 시 이 메소드 안에서만 수정하면 되므로 유지보수가 용이해진다.
    public void updateIntroduction(String introduction){
        this.introduction = introduction;
    }
}