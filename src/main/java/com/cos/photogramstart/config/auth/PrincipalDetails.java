package com.cos.photogramstart.config.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.cos.photogramstart.domain.user.User;

import lombok.Data;

@Data  // user 의 getter, setter 만들기 위해서
public class PrincipalDetails implements UserDetails, OAuth2User {
	private static final long serialVersionUID = 1L;
	
	private User user;
	private Map<String, Object> attributes;
	
	
	public PrincipalDetails(User user) {
		this.user = user;
	}
	
	// 오버 로딩 처리(PrincipalDetails 와 페북 로긴 유저용(OAuthDetails ) 를 분리안하고 처리할 때 구별하기 위해서
	public PrincipalDetails(User user, Map<String, Object> attributes) {
		this.user = user;
	}
	
	// 권한 : 한개가 아닐 수 있음 (3개 이상의 권한)
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		Collection<GrantedAuthority> collector = new ArrayList<>();
		// 자바에서는 매개변수에 함수가 일급 객체가 아니라  못넘김,  함수로 넘기고 싶으면 인터페이스/클래스 오브젝트로 넘김 -> 람다식으로 넘김
		collector.add(() -> { return user.getRole(); });
		return collector;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;  // {id=27, name=쌀, email=aa@nate.com} , OAuth 로그인 한 경우 존재
	}

	@Override
	public String getName() {
		 return (String) attributes.get("name"); // , OAuth 로그인 한 경우 존재
	}

}
