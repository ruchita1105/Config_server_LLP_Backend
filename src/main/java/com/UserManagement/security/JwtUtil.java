package com.UserManagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {


    @Value("${jwt.secret}")
    private String secret;

    private Key signingKey;

    private static final long ACCESS_TOKEN_VALIDITY =  60 * 1000; // 2 minutes

    private static final long REFRESH_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // 1 day

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder().encode(secret.getBytes());// string to byte
        signingKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());  // byte to singing key
    }

    private Key getSigningKey() {
        return signingKey;
    }

    // ✅ Generate Access Token with dynamic role
    public String generateAccessToken(String username,long userId, String role) {
        return createToken(username, userId, role, ACCESS_TOKEN_VALIDITY);
    }

    // ✅ Generate Refresh Token with dynamic role
    public String generateRefreshToken(String username,long userId, String role) {
        return createToken(username,  userId,role, REFRESH_TOKEN_VALIDITY);
    }

    // ✅ Create token with role as claim
    private String createToken(String subject, long userId , String role, long validity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + role.toUpperCase()); // ✅ Fix: Add ROLE_ prefix
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Validate token
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        boolean isExpired = isTokenExpired(token);
        return (tokenUsername.equals(username) && !isExpired);
    }

    // ✅ Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ✅ Extract role from token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // ✅ General claim extractor
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ✅ Extract all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Check token expiry
    private Boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
