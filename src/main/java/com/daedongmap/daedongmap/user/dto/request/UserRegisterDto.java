package com.daedongmap.daedongmap.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRegisterDto {

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickName;
    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phoneNumber;

    private String profileImage;

    @Size(min = 8, message = "비밀번호는 8자리 이상입니다!")
    private String password;

    @Builder
    public UserRegisterDto(String email, String nickName, String phoneNumber, String password, String profileImage) {
        this.email = email;
        this.nickName = nickName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.profileImage = profileImage;
    }
}
