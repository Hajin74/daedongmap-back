package com.daedongmap.daedongmap.user;

import com.daedongmap.daedongmap.user.controller.UserController;
import com.daedongmap.daedongmap.user.domain.Authority;
import com.daedongmap.daedongmap.user.domain.Users;
import com.daedongmap.daedongmap.user.dto.request.UserLoginDto;
import com.daedongmap.daedongmap.user.dto.request.UserRegisterDto;
import com.daedongmap.daedongmap.user.dto.response.JwtTokenDto;
import com.daedongmap.daedongmap.user.service.UserService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
//@ContextConfiguration(classes = DaedongmapApplication.class)
public class UserControllerTests {

    @InjectMocks
    UserController userController;
    @Mock
    private UserService userService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MockMvc mockMvc;

    private final UserRegisterDto userRegisterDto = UserRegisterDto.builder()
            .email("test@test.com")
            .nickName("test nickname")
            .phoneNumber("01015771577")
            .profileImage("test profile")
            .build();

    private final Users testUser = Users.builder()
            .id(1L)
            .nickName(userRegisterDto.getNickName())
            .status("안녕하세요! 반갑습니다!")
            .email(userRegisterDto.getEmail())
            .webSite("아직 연결된 외부 사이트가 없습니다.")
            .phoneNumber(userRegisterDto.getPhoneNumber())
            .profileImage(userRegisterDto.getProfileImage())
            .password(userRegisterDto.getPassword())
            .isMember(true)
            .role(Collections.singletonList(Authority.builder().role("ROLE_USER").build()))
            .build();


    @Test
    @DisplayName("사용자 등록 테스트")
    public void registerUser() throws Exception {
        // given
        String json = objectMapper.writeValueAsString(userRegisterDto);

        // when
        MvcResult result = mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();

        // then
        assertEquals(testUser.getNickName(), response);
    }

//    @Test
//    @DisplayName("사용자 로그인 테스트")
//    public void loginUser() throws Exception {
//        // given
//        UserLoginDto dto = new UserLoginDto(testUser.getEmail(), testUser.getPassword());
//        String json = objectMapper.writeValueAsString(dto);
//
//        // when
//        MvcResult result = mockMvc.perform(post("/api/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(json))
//                .andExpect(status().isAccepted())
//                .andReturn();
//
//        // then
//        assertEquals(result.getResponse().getContentAsString(), JwtTokenDto.class.toString());
//    }
}