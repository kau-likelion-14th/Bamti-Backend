package likelion14th.lte.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.user.dto.request.CreateTestUserRequest;
import likelion14th.lte.user.dto.response.UserProfileResponse;
import likelion14th.lte.user.service.UserProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileController{

    public final UserProfileService userProfileService;

    // [Q9. Controller 내부에서 userRepository.findById()를 직접 호출해서 유저를 찾지 않고,
    // 반드시 userProfileService를 호출하여 작업을 위임해야 하는 이유는 무엇인가요? (단일 책임 원칙 관점)]
    // 답변: 직접 호출하면 Controller가 HTTP 요청/응답 처리와 비즈니스 로직을 동시에 담당하게 되면서 단일 책임 원칙을 위반한다.
    //      이를 해결하기 위해 userProfileService에 작업을 위임하면 로직이 바뀌어도 Controller를 수정할 필요가 없고 각 계층별 테스트를 할 수 있다.
    @GetMapping
    public ApiResponse<UserProfileResponse> getUserProfile(@RequestParam Long userId){
        UserProfileResponse response = userProfileService.getUserProfile(userId);
        return ApiResponse.onSuccess(SuccessCode.USER_INFO_GET_SUCCESS, response);
    }

    @PostMapping
    public ApiResponse<UserProfileResponse> createTestUser(
            // [Q10. 클라이언트가 보낸 JSON 텍스트 데이터가 어떻게 자바 객체인 CreateTestUserRequest로
            // 변환 되는지앞의 어노테이션과 연관 지어 설명해 보세요.]
            // 답변: @RequestBody 어노테이션이 있으면 JSON의 키 이름을 CreateTestUserRequest의
            //      필드명과 매칭시겨 값이 채워진 자바 객체로 변환해준다.
            @RequestBody CreateTestUserRequest request
    ){
        UserProfileResponse response = userProfileService.createTestUser(request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }
}