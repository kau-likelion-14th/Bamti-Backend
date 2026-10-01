package likelion14th.lte.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.api.SuccessCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.login.dto.request.KakaoCodeRequest;
import likelion14th.lte.login.dto.response.AuthResponse;
import likelion14th.lte.login.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "Auth", description = "Kakao OAuth 로그인 및 JWT 인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${auth.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${auth.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${jwt.refresh-exp-ms:1209600000}")
    private long refreshExpMs;

    @PostMapping("/kakao")
    @SecurityRequirements
    @Operation(summary = "카카오 로그인", description = "인가 코드로 Kakao 사용자 정보를 확인하고 우리 Access/Refresh Token을 발급합니다.")
    public ApiResponse<AuthResponse> kakaoLogin(
            @Valid @RequestBody KakaoCodeRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.handleKakaoCode(request.getCode());

        httpResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                createRefreshTokenCookie(response.getRefreshToken()).toString()
        );

        return ApiResponse.onSuccess(SuccessCode.USER_LOGIN_SUCCESS, response);
    }

    @PostMapping("/reissue")
    @SecurityRequirements
    @Operation(summary = "Access Token 재발급", description = "HttpOnly 쿠키의 Refresh Token으로 새 Access Token을 발급합니다.")
    public ApiResponse<String> reissue(
            @Parameter(hidden = true)
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new GeneralException(ErrorCode.TOKEN_INVALID);
        }

        String newAccessToken = authService.reissueAccessToken(refreshToken);
        return ApiResponse.onSuccess(SuccessCode.USER_REISSUE_SUCCESS, newAccessToken);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "DB의 Refresh Token과 브라우저의 Refresh Token 쿠키를 삭제합니다.")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletResponse httpResponse
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        authService.logout(userId);

        httpResponse.addHeader(
                HttpHeaders.SET_COOKIE,
                deleteRefreshTokenCookie().toString()
        );

        return ApiResponse.onSuccess(SuccessCode.USER_LOGOUT_SUCCESS, null);
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원탈퇴", description = "User를 삭제하고 Refresh Token 쿠키를 삭제합니다.")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletResponse response
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        authService.withdraw(userId);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                deleteRefreshTokenCookie().toString()
        );

        return ApiResponse.onSuccess(SuccessCode.USER_DELETE_SUCCESS, null);
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(Duration.ofMillis(refreshExpMs))
                .path("/")
                .build();
    }

    // 생성할 때와 path / secure / sameSite 동일하게 맞춰야 삭제됨
    private ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(Duration.ZERO)
                .path("/")
                .build();
    }
}
