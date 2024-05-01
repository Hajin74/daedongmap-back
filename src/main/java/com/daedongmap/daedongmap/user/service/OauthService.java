package com.daedongmap.daedongmap.user.service;

import com.daedongmap.daedongmap.exception.CustomException;
import com.daedongmap.daedongmap.exception.ErrorCode;
import com.daedongmap.daedongmap.security.jwt.TokenProvider;
import com.daedongmap.daedongmap.user.domain.Authority;
import com.daedongmap.daedongmap.user.domain.Users;
import com.daedongmap.daedongmap.user.dto.request.UserRegisterDto;
import com.daedongmap.daedongmap.user.dto.response.JwtTokenDto;
import com.daedongmap.daedongmap.user.dto.response.OAuthTokenResponseDto;
import com.daedongmap.daedongmap.user.dto.response.OAuthUserInfoDto;
import com.daedongmap.daedongmap.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthService {

    private final UserDetailService userDetailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final UserService userService;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String NAVER_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String NAVER_REDIRECT;

    @Value("${spring.security.oauth2.provider.naver.user-info-uri}")
    private String NAVER_USER_INFO;

    private String type;
    private String clientId;
    private String redirectUri;
    
    // TODO: OAuth 사용자 DB 구축이 필요할까
    public JwtTokenDto signUpAndLogin(String code, String type) {

        if(type.equals("naver")) {
            clientId = NAVER_CLIENT_ID;
            redirectUri = NAVER_REDIRECT;
        } else {
            return null;
        }

        OAuthTokenResponseDto token = getToken(code);

        OAuthUserInfoDto profile = getUserProfile(token);

        System.out.println(profile);

        Users user = userService.findUserByEmail(profile.getEmail());

        if(user != null) {
            tokenProvider.createNewAccessToken(user, user.getRoles());
        }

        Users newUser = Users.builder()
                .nickName(profile.getNickName())
                .status("안녕하세요! 반갑습니다!")
                .email(profile.getEmail())
                .webSite("아직 연결된 외부 사이트가 없습니다.")
                .phoneNumber(profile.getPhoneNumber())
                .profileImage("기본프로필 이미지 링크")
                .role(Collections.singletonList(Authority.builder().role("ROLE_USER").build()))
                .build();
        
        return tokenProvider.createNewAccessToken(newUser, newUser.getRoles());

//        {
//            "email": "\"sanghyun123452@gmail.com\"",
//            "nickName": "\"평범한사람\"",
//            "phoneNumber": "\"010-1234-1234\""
//        }
    }

    public OAuthTokenResponseDto getToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("PRIVATE_TOKEN", "xyz");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("client_secret", NAVER_CLIENT_SECRET);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response;

        response = restTemplate.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                request,
                String.class);

        String responseBody = response.getBody();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return OAuthTokenResponseDto.builder()
                    .accessToken(jsonNode.get("access_token").asText())
                    .refreshToken(jsonNode.get("refresh_token").asText())
                    .build();

        } catch(Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private OAuthUserInfoDto getUserProfile(OAuthTokenResponseDto token) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token.getAccessToken());

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> request = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate
                .postForEntity(NAVER_USER_INFO, request, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return OAuthUserInfoDto.builder()
                    .email(String.valueOf(jsonNode.get("response").get("email")))
                    .nickName(String.valueOf(jsonNode.get("response").get("nickname")))
                    .phoneNumber(String.valueOf(jsonNode.get("response").get("mobile")))
                    .build();
        } catch(Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
