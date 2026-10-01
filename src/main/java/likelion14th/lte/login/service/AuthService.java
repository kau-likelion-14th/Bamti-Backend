package likelion14th.lte.login.service;

import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.login.client.KakaoClient;
import likelion14th.lte.login.domain.RefreshToken;
import likelion14th.lte.login.dto.response.AuthResponse;
import likelion14th.lte.login.jwt.JwtProvider;
import likelion14th.lte.login.repository.RefreshTokenRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    // code -> Kakao 토큰 -> Kakao 사용자 정보 -> User 조회/생성 -> 우리 토큰 발급
    public AuthResponse handleKakaoCode(String code) {
        String kakaoAccessToken = kakaoClient.getAccessToken(code);
        JsonNode kakaoUserInfo = kakaoClient.getUserInfo(kakaoAccessToken);

        String providerId = kakaoUserInfo.path("id").asText(null);
        String username = kakaoUserInfo.path("kakao_account")
                .path("profile")
                .path("nickname")
                .asText("카카오 유저");

        User user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .providerId(providerId)
                                .username(username)
                                .userTag(createUniqueUserTag())
                                .build()
                ));

        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        Long refreshTokenExpiration = jwtProvider.getRefreshTokenExpiration();

        saveOrUpdateRefreshToken(user, refreshToken, refreshTokenExpiration);
        return AuthResponse.from(user, accessToken, refreshToken);
    }

    private void saveOrUpdateRefreshToken(User user, String token, Long expiration) {
        refreshTokenRepository.findByUser(user).ifPresentOrElse(
                existing -> existing.updateToken(token, expiration),
                () -> refreshTokenRepository.save(
                        RefreshToken.builder()
                                .user(user)
                                .refreshToken(token)
                                .refreshTokenExpiration(expiration)
                                .build()
                )
        );
    }

    @Transactional(readOnly = true)
    public String reissueAccessToken(String refreshToken) {
        Long userId;
        try {
            userId = jwtProvider.validateRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        RefreshToken savedToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new GeneralException(ErrorCode.WRONG_REFRESH_TOKEN));

        // DB 값과 쿠키 값이 같은지 확인
        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        return jwtProvider.createAccessToken(user.getId());
    }

    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        // [문서와 다른 부분]
        // 문서 코드: refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        // User.refreshToken(cascade = ALL)이 같은 객체를 계속 참조하고 있으면
        // flush 시점에 삭제가 취소될 수 있어서, 연관관계를 끊고 orphanRemoval로 삭제.
        user.removeRefreshToken();
    }

    private String createUniqueUserTag() {
        return "KAKAO" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));

        // cascade로 RefreshToken / Follow / SavedSong 같이 삭제
        userRepository.delete(user);
    }
}
