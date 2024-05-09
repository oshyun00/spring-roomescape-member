package roomescape.argumentresolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.service.MemberService;
import roomescape.service.request.LoginMember;
import roomescape.web.exception.AuthenticationException;

public class TokenValueMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final MemberService memberService;

    public TokenValueMethodArgumentResolver(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LoginMember.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Cookie[] cookies = request.getCookies();
        String tokenValue = getTokenValue(cookies);

        return memberService.getLoginMember(tokenValue);
    }

    private String getTokenValue(Cookie[] cookies) {
        validateCookie(cookies);
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private void validateCookie(Cookie[] cookies) {
        if (cookies == null) {
            throw new AuthenticationException("로그인된 회원 정보가 없습니다.");
        }
    }
}
