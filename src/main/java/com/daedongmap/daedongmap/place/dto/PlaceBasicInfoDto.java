package com.daedongmap.daedongmap.place.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class PlaceBasicInfoDto {
    private Long id;
    private Long kakaoPlaceId;
    private String placeName;
    private String placeUrl;
    private String categoryName;
    private String addressName;
    private String roadAddressName;
    private String phone;

    private Double x;
    private Double y;

    private float averageRating;

    private String reviewImagePath;
    private String reviewCnt;
}
