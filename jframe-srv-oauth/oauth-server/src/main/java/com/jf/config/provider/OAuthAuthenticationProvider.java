package com.jf.config.provider;

import com.jf.service.OUserDetailService;
import com.jf.database.model.OUserExtDetail;
import com.jf.database.model.UserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * Description: 自定义的权限校验
 * User: admin
 * Date: 2018-10-31
 * Time: 16:14
 */
@Component
public class OAuthAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private OUserDetailService oUserDetailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 自定义验证方式
     *
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 验证码等校验
        OUserExtDetail detail = (OUserExtDetail) authentication.getDetails();
        if (!detail.getVerify().equalsIgnoreCase(detail.getVerifySession())) {
            throw new BadCredentialsException("验证码错误");
        }

        // 用户名密码校验
        UserDetail userDetail = (UserDetail) oUserDetailService.loadUserByUsername(authentication.getName());
        if (!userDetail.getUsername().equals(authentication.getName()) ||
                !passwordEncoder.matches(String.valueOf(authentication.getCredentials()), userDetail.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        Collection<? extends GrantedAuthority> authorities = userDetail.getAuthorities();
        // 这里的pricipal可以换成userid
        return new UsernamePasswordAuthenticationToken(userDetail.getUsername(), userDetail.getPassword(), authorities);
    }

    @Override
    public boolean supports(Class<?> cls) {
        return true;
    }
}
