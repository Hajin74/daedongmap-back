package com.daedongmap.daedongmap.follow.service;

import com.daedongmap.daedongmap.alarm.service.AlarmService;
import com.daedongmap.daedongmap.exception.CustomException;
import com.daedongmap.daedongmap.exception.ErrorCode;
import com.daedongmap.daedongmap.follow.model.Follow;
import com.daedongmap.daedongmap.follow.repository.FollowRepository;
import com.daedongmap.daedongmap.likes.repository.LikeRepository;
import com.daedongmap.daedongmap.likes.service.LikeService;
import com.daedongmap.daedongmap.place.domain.Place;
import com.daedongmap.daedongmap.review.domain.Review;
import com.daedongmap.daedongmap.review.repository.ReviewRepository;
import com.daedongmap.daedongmap.user.domain.Authority;
import com.daedongmap.daedongmap.user.domain.Users;
import com.daedongmap.daedongmap.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private FollowRepository followRepository;
    @Mock
    private AlarmService alarmService;
    @Mock
    private LikeService likeService;
    @InjectMocks
    private FollowService followService;


    @Test
    @DisplayName("팔로우 하기 (성공)")
    void doFollow_success() {
        // given
        Users mockUser1 = Users.builder()
                .id(1L)
                .nickName("mock-user-1")
                .isMember(true)
                .role(Collections.singletonList(Authority.builder().role("ROLE_USER").build()))
                .build();

        Users mockUser2 = Users.builder()
                .id(2L)
                .nickName("mock-user-2")
                .isMember(true)
                .role(Collections.singletonList(Authority.builder().role("ROLE_USER").build()))
                .build();

        Follow mockFollow = new Follow(mockUser1, mockUser2);

        lenient().when(userRepository.findById(mockUser1.getId())).thenReturn(Optional.of(mockUser1));
        lenient().when(userRepository.findById(mockUser2.getId())).thenReturn(Optional.of(mockUser2));
        lenient().when(followRepository.findByFollowerAndFollowing(mockUser1, mockUser2)).thenReturn(null);

        // when
        followService.doFollow(mockUser1.getId(), mockUser2.getId());

        // then
        verify(followRepository, times(1)).save(any(Follow.class));
        verify(alarmService, times(1)).sendToClient(mockFollow.getFollowing().getId(), "You have a new follower - " + mockFollow.getFollower().getId());
    }

    @Test
    @DisplayName("팔로우 하기 (실패 - 이미 팔로우를 한 경우)")
    void doFollow_alreadyFollowed_failure() {
        // given
        Users mockUser1 = Users.builder()
                .id(1L)
                .nickName("mock-user-1")
                .isMember(true)
                .role(Collections.singletonList(Authority.builder().role("ROLE_USER").build()))
                .build();

        Users mockUser2 = Users.builder()
                .id(2L)
                .nickName("mock-user-2")
                .isMember(true)
                .role(Collections.singletonList(Authority.builder().role("ROLE_USER").build()))
                .build();

        Follow mockFollow = new Follow(mockUser1, mockUser2);

        lenient().when(userRepository.findById(mockUser1.getId())).thenReturn(Optional.of(mockUser1));
        lenient().when(userRepository.findById(mockUser2.getId())).thenReturn(Optional.of(mockUser2));
        lenient().when(followRepository.findByFollowerAndFollowing(mockUser1, mockUser2)).thenReturn(mockFollow);

        // when
        CustomException exception = assertThrows(
                CustomException.class, () -> followService.doFollow(mockUser1.getId(), mockUser2.getId())
        );

        // then
        assertEquals(ErrorCode.FOLLOW_DUPLICATED, exception.getErrorCode());
        verify(followRepository, never()).save(any(Follow.class));
        verify(alarmService, never()).sendToClient(anyLong(), anyString());
    }

    @Test
    @DisplayName("팔로우 하기 (실패 - 본인을 팔로우한 경우)")
    void doFollow_myself_failure() {
        // given
        Users mockUser = Users.builder()
                .id(1L)
                .nickName("mock-user")
                .isMember(true)
                .role(Collections.singletonList(Authority.builder().role("ROLE_USER").build()))
                .build();

        when(userRepository.findById(mockUser.getId())).thenReturn(Optional.of(mockUser));

        // when
        CustomException exception = assertThrows(CustomException.class, () -> followService.doFollow(mockUser.getId(), mockUser.getId()));

        // then
        verify(followRepository, never()).save(any(Follow.class));
        verify(alarmService, never()).sendToClient(anyLong(), anyString());
        assertEquals(ErrorCode.FOLLOW_MYSELF_NOW_ALLOWED, exception.getErrorCode());
    }
}