package likelion14th.lte.login.jwt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JwtProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshTokenDecoder;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtProvider(
            JwtEncoder jwtEncoder,
            @Qualifier("refreshTokenDecoder") JwtDecoder refreshTokenDecoder,
            @Value("${jwt.access-exp-ms:3600000}") long accessExpMs,
            @Value("${jwt.refresh-exp-ms:1209600000}") long refreshExpMs
    ) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenDecoder = refreshTokenDecoder;
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    public String createAccessToken(Long userId) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusMillis(accessExpMs))
                .claim("type", "ACCESS")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(now.plusMillis(refreshExpMs))
                .claim("type", "REFRESH")
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    // DB에 저장할 Refresh Token 만료 시각 (epoch ms)
    public Long getRefreshTokenExpiration() {
        return System.currentTimeMillis() + refreshExpMs;
    }

    // 서명 / 만료 / type=REFRESH 검증 후 userId 반환
    public Long validateRefreshToken(String token) {
        Jwt jwt = refreshTokenDecoder.decode(token);
        return Long.parseLong(jwt.getSubject());
    }
}
