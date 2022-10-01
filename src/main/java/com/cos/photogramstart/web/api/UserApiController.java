package com.cos.photogramstart.web.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.cos.photogramstart.config.auth.PrincipalDetails;
import com.cos.photogramstart.domain.user.User;
import com.cos.photogramstart.handler.ex.CustomValidationApiException;
import com.cos.photogramstart.service.SubscribeService;
import com.cos.photogramstart.service.UserService;
import com.cos.photogramstart.web.dto.CMRespDto;
import com.cos.photogramstart.web.dto.subscribe.SubscribeDto;
import com.cos.photogramstart.web.dto.user.UserUpdateDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class UserApiController {
	
	private final UserService userService;
	private final SubscribeService subscribeService;
	
	@PutMapping("/api/user/{principalId}/profileImageUrl")
	public ResponseEntity<?> profileImageUrlUpdate(@PathVariable int principalId, MultipartFile profileImageFile,
			@AuthenticationPrincipal PrincipalDetails principalDetails) {  // form tag 컬럼 이름
		User userEntity = userService.회원프로필사진변경(principalId, profileImageFile);
		principalDetails.setUser(userEntity);  // 세션 변경
		return new ResponseEntity<>(new CMRespDto<>(1, "프로필사진변경 성공", null), HttpStatus.OK);
	}
	
	@GetMapping("/api/user/{pageUserId}/subscribe")
	public ResponseEntity<?> subscribeList(
			@PathVariable int pageUserId, 
			@AuthenticationPrincipal PrincipalDetails principalDetails) {
		
		List<SubscribeDto> subscribeDto = subscribeService.구독리스트(principalDetails.getUser().getId(), pageUserId);
		return new ResponseEntity<>(new CMRespDto<>(1, "구독자 정보 리스트 불러오기 성공", subscribeDto), HttpStatus.OK);
	}
	
	@PutMapping("/api/user/{userId}")
	public CMRespDto<?> update(
			@PathVariable int userId, 
			@Valid UserUpdateDto userUpdateDto,
			BindingResult bindingResult,  // 꼭 @Valid 가 적혀있는 다음 파라미터에 적음
			@AuthenticationPrincipal PrincipalDetails principalDetails) {
		
		if (bindingResult.hasErrors()) {
			Map<String, String> errorMap = new HashMap<>();
				
			for (FieldError error : bindingResult.getFieldErrors()) {
				errorMap.put(error.getField(), error.getDefaultMessage());
			}
			throw new CustomValidationApiException("유효성 검사 실패함", errorMap);
		} else {
			System.out.println(userUpdateDto);
			
			User userEntity = userService.회원수정(userId,  userUpdateDto.toEntity());
			principalDetails.setUser(userEntity);  // 세션정보 변경하였기 때문에 변경된 정보가 유지됨, 생략시 정보 변경 반영 안됨
			
			// 응답시에 userEntity의 모든 getter 함수가 호출되고 이것을 JSON 으로 파싱하여 응답되므로 무한 호출 주의
			// 내부적으로 MessageConverter 가 모든 getter 호출함 : getImages 호출시 에러
			// jpa 주의점 sysout 시 toString() 하게 되면 연관된 객체들에서 무한참조 발생가능하므로 주의
			return new CMRespDto<>(1, "회원수정완료", userEntity);   // 에러 난 부분 HttpMessageNotWritableException
		}
	}
}