package com.hc.workmate.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hc.workmate.mastertenant.service.MasterTenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hc.workmate.security.service.impl.UserDetailsServiceImpl;
import com.hc.workmate.util.Constants;


public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private MasterTenantService masterTenantService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            logger.info("STAND HERE00000000000000");
            if (!isUriValid(request)) {
                if (!checkAuthorization(request)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                String jwt = parseJwt(request);
                if (jwt != null && jwtUtils.getClaimsJwsViaJwtToken(jwt) != null) {
                    String username = jwtUtils.getClaimsJwsViaJwtToken(jwt).getBody().getSubject();
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }


    private Boolean checkAuthorization(HttpServletRequest request) {
        return request.getHeader(Constants.HEADER_AUTHORIZATION) != null ? true : false;
    }

    private Boolean isUriValid(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        logger.info("requestURI: " + requestURI);
        return requestURI.equals(Constants.LOGIN_URI)
        		|| requestURI.equals(Constants.SIGNUP_URI)
                || requestURI.equals(Constants.TOKEN_URI)
                || requestURI.equals(Constants.ACTIVE_USER_URI)
                || requestURI.contains(Constants.FORGOT_PASSWORD_URI)
                ? true : false;
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader(Constants.HEADER_AUTHORIZATION);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(Constants.ACCESS_TOKEN_TYPE)) {
            return headerAuth.substring(Constants.ACCESS_TOKEN_TYPE.length() + 1, headerAuth.length());
        }
        return null;
    }

}
