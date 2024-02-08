package com.tappsso.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configures our application with Spring Security to restrict access to our API endpoints.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*
        This is where we configure the security required for our endpoints and setup our app to serve as
        an OAuth2 Resource Server, using JWT validation.
        */


        return http
                .authorizeHttpRequests((authorize) -> authorize
                        .mvcMatchers("/api/test").permitAll()
                )
                .cors(withDefaults())
                .build();
    }
}