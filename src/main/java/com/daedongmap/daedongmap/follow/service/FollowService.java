package com.daedongmap.daedongmap.follow.service;

import com.daedongmap.daedongmap.alarm.service.AlarmService;
import com.daedongmap.daedongmap.exception.CustomException;
import com.daedongmap.daedongmap.exception.ErrorCode;
import com.daedongmap.daedongmap.follow.dto.FollowerDto;
import com.daedongmap.daedongmap.follow.dto.FollowingDto;
import com.daedongmap.daedongmap.follow.model.Follow;
import com.daedongmap.daedongmap.follow.repository.FollowRepository;
import com.daedongmap.daedongmap.user.domain.Users;
import com.daedongmap.daedongmap.user.dto.UserBasicInfoDto;
import com.daedongmap.daedongmap.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;

    @Transactional
    public void doFollow(Long followerId, Long followingId) {
        Users follower = getUserById(followerId);
        Users following = getUserById(followingId);

        // 자기 자신은 팔로우 할 수 없음
        if (followerId.equals(followingId)) {
            throw new CustomException(ErrorCode.FOLLOW_MYSELF_NOW_ALLOWED);
        }

        // 이미 팔로우 중인지 확인
        if (isAlreadyFollowing(follower, following)) {
            throw new CustomException(ErrorCode.FOLLOW_DUPLICATED);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);
        alarmService.sendToClient(followingId, "You have a new follower - " + followerId);
    }

    @Transactional
    public void unFollow(Long followerId, Long followingId) {
        Users follower = getUserById(followerId);
        Users following = getUserById(followingId);

        // 자기 자신은 팔로우 할 수 없음
        if (followerId.equals(followingId)) {
            throw new CustomException(ErrorCode.UNFOLLOW_MYSELF_NOW_ALLOWED);
        }

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following);
        if (follow == null) {
            throw new CustomException(ErrorCode.FOLLOW_NOT_FOUND);
        }

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public List<FollowingDto> getFollowingList(Long userId) {
        // user 가 팔로워인 입장
        Users user = getUserById(userId);
        List<Follow> followingList = followRepository.findAllByFollower(user);

        // 팔로잉한 상대가 역으로 유저를 팔로우를 하는지 확인
        List<FollowingDto> followingDtoList = new ArrayList<>();
        for (Follow follow : followingList) {
            boolean isFollower = followRepository.existsByFollowerAndFollowing(follow.getFollowing(), user);
            UserBasicInfoDto followingUserInfo = new UserBasicInfoDto(follow.getFollowing());
            FollowingDto followingDto = new FollowingDto(followingUserInfo, isFollower);
            followingDtoList.add(followingDto);
        }

        return followingDtoList;
    }

    @Transactional(readOnly = true)
    public List<FollowerDto> getFollowerList(Long userId) {
        // user 가 팔로잉 당한 입장
        Users user = getUserById(userId);
        List<Follow> followerList = followRepository.findAllByFollowing(user);

        // 유저를 팔로우하는 상대를 유저도 팔로잉하는지 확인
        List<FollowerDto> followerDtoList = new ArrayList<>();
        for (Follow follow : followerList) {
            boolean isFollowing = followRepository.existsByFollowerAndFollowing(user, follow.getFollower());
            UserBasicInfoDto followerInfo = new UserBasicInfoDto(follow.getFollower());
            FollowerDto followerDto = new FollowerDto(followerInfo, isFollowing);
            followerDtoList.add(followerDto);
        }

        return followerDtoList;
    }

    private Users getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public boolean isAlreadyFollowing(Users follower, Users following) {
        Follow follow = followRepository.findByFollowerAndFollowing(follower, following);
        return follow != null;
    }

}
