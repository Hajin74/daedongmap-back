package com.daedongmap.daedongmap.user.controller;

import com.daedongmap.daedongmap.user.dto.response.JwtTokenDto;
import com.daedongmap.daedongmap.user.service.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/login/oauth2")
public class OAuthController {

    private final OauthService oauthService;
    private String type;

    @GetMapping("/naver")
    public ResponseEntity<JwtTokenDto> naverLogin(@RequestParam(name = "code") String code) {

        type = "naver";

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(oauthService.signUpAndLogin(code, type));
    }

    @GetMapping("/kakao")
    public ResponseEntity<JwtTokenDto> kakaoLogin(@RequestParam(name = "code") String code) {

        type = "kakao";

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(oauthService.signUpAndLogin(code, type));
    }

    @GetMapping("/google")
    public ResponseEntity<JwtTokenDto> googleLogin(@RequestParam(name = "code") String code) {

        type = "google";

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(oauthService.signUpAndLogin(code, type));
    }
}
