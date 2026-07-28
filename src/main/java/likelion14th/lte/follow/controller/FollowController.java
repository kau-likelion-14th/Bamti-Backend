package likelion14th.lte.follow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion14th.lte.follow.dto.FollowUserRequest;
import likelion14th.lte.follow.dto.FollowUserResponse;
import likelion14th.lte.follow.service.FollowService;
import likelion14th.lte.global.api.ApiResponse;
import likelion14th.lte.global.api.SuccessCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/follow")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Tag(name = "팔로우 API", description = "팔로우 추가 및 삭제, 조회를 담당하는 api입니다.")
public class FollowController {

    private final FollowService followService;

    @PostMapping
    @Operation(summary = "팔로우 추가", description = "json 요청에 유저아이디를 전달해주면 팔로우 추가를 해줍니다.")
    public ApiResponse<FollowUserResponse> addFollow(
            @RequestParam Long userId,
            @RequestBody FollowUserRequest followUserRequest
    ){
        FollowUserResponse response = followService.followUser(userId, followUserRequest.getToUserId());
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_ADD_SUCCESS, response);
    }

    @DeleteMapping
    @Operation(summary = "언팔로우", description = "유저 id를 통해 언팔로우를 진행합니다")
    public ApiResponse<Void> deleteFollow(
            @RequestParam Long userId,
            @RequestBody FollowUserRequest followUserRequest){
        followService.unfollowUser(userId, followUserRequest.getToUserId());
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_DELETE_SUCCESS, null);
    }

    @GetMapping("/followers")
    public ApiResponse<List<FollowUserResponse>> getFollows(
            @RequestParam Long userId
    ){
        List<FollowUserResponse> response = followService.getFollowers(userId);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_LIST_GET_SUCCESS, response);
    }

    @GetMapping("/followings")
    public ApiResponse<List<FollowUserResponse>> getFollowings(
            @RequestParam Long userId
    ){
        List<FollowUserResponse> response = followService.getFollowings(userId);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_LIST_GET_SUCCESS, response);
    }

    @GetMapping
    @Operation(summary = "팔로우 가능한 유저를 조회합니다.", description = "팔로우 가능한 유저를 페이징하며 조회합니다. " +
            "쿼리 파라미터로 page, size, sort를 전달할 수 있습니다. " +
            "sort 파라미터는 선택사항이며, 형식: sort=id,DESC 또는 sort=username,ASC (쉼표로 구분). " +
            "정렬 가능한 필드: id, username, userTag, createdAt, updatedAt")
    public ApiResponse<Page<FollowUserResponse>> getCanFollowUsers(
            @RequestParam Long userId,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ){
        Page<FollowUserResponse> responses = followService.getCanFollowers(userId, pageable);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_SEARCH_SUCCESS, responses);
    }

    @GetMapping("/search")
    @Operation(summary = "팔로우 가능한 유저 검색")
    public ApiResponse<Page<FollowUserResponse>> getSearchFollows(
            @RequestParam Long userId,
            @RequestParam String nickname,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable
    ){
        Page<FollowUserResponse> responses = followService.searchCanFollowers(userId, nickname, pageable);
        return ApiResponse.onSuccess(SuccessCode.FOLLOW_SEARCH_SUCCESS, responses);
    }
}