package likelion14th.lte.user.service;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileService{

    // [Q5. Service 안에서 new UserRepository() 로 객체를 직접 생성하지 않고,
    // 외부에서 의존성 주입(DI)을 받는 이유는 무엇인가요? (결합도와 단위 테스트 관점)]
    // 답변: Repository 구현체가 바뀌어도 Service 코드는 변경이 불필요해져 결합도가 감소한다.
    private final UserRepository userRepository;

    // [Q6. (코딩 문제) 만약 클래스 위의 @RequiredArgsConstructor를 지운다면,
    // 우리가 직접 작성해야 할 의존성 주입용 자바 '생성자' 코드는 어떤 모습일까요? 아래에 직접 코딩해 보세요.]
    /*
       public UserProfileService(UserRepository userRepository) {
            this.userRepository = userRepository;
       }
    */

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse createTestUser(CreateTestUserRequest request){

        // [Q7. 일반적인 생성자 new User(name, intro, tag) 방식을 쓰지 않고,
        // User.builder()...build() 라는 '빌더 패턴'을 사용하여 객체를 조립했을 때 얻는 장점은 무엇인가요?]
        // 답변: 필요한 필드만 선택적으로 세팅할 수 있고 순서를 지키지 않아도 된다.
        User newUser = User.builder()
                .username(request.getUsername())
                .introduction(request.getIntroduction())
                .userTag(request.getUserTag())
                .build();

        User savedUser;
        try{
            // [Q8. 데이터를 저장하는 이 메서드 위에 @Transactional이 반드시 붙어야 하는 이유는 무엇인가요?
            // (저장 도중 DB 서버가 끊겼을 때의 상황을 가정해서 설명하세요)]
            // 답변: 저장 도중 DB 서버가 끊기면 해당 Transaction 내의 작업이 취소 및 복구된다.
            //      그러나 @Transactional이 없으면 일부 데이터만 불완전하게 저장된다. 이를 방지하기 위해 반드시 필요하다.
            savedUser = userRepository.save(newUser);
        } catch (Exception e){
            throw new GeneralException(ErrorCode.BAD_REQUEST);
        }
        return UserProfileResponse.from(savedUser);
    }
}