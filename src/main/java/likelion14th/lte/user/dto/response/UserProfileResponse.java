package likelion14th.lte.user.dto.response;

import likelion14th.lte.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfileResponse {
    private String Username;
    private String profileImageUrl;
    private String introduction;

    // [Q4. Controller가 DB에서 꺼낸 원본 Entity(User)를 클라이언트 화면에 그대로 반환하지 않고,
    // 굳이 from() 메서드를 통해 DTO로 한번 변환해서 내보내는 핵심적인 이유 2가지는 무엇인가요?]
    // 답변: 1. 보안: User Entity에는 password, s3ImageKey 등 민감한 정보가 포함된다. 그러나 DTO 변환으로 필요한 데이터만 내보낼 수 있게 된다.
    //      2. 유지보수: 데이터 형태가 바뀌어도 DTO만 수정하면 되고 Entity까지 건드릴 필요가 없고 독립적으로 변경 가능하다.
    public static UserProfileResponse from (User user) {
        return new UserProfileResponse(
                user.getUsername() + "#" + user.getUserTag(),
                user.getUserTag(),
                user.getIntroduction()
        );
    }
}
