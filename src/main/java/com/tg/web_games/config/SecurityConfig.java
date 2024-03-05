package com.tg.web_games.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

   private final JwtAuthFilter jwtAuthFilter;
   private final AuthenticationProvider authenticationProvider;

   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
      return http.csrf(csrf ->  csrf.disable())
              .authorizeHttpRequests(auth -> auth
                      .requestMatchers("/api/v1/user/**","/home")
                      .permitAll()
                      .anyRequest().authenticated())
              .sessionManagement( session -> session
                      .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
              .authenticationProvider(authenticationProvider)
              .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
              .build();
   }

   @Bean
   public CorsConfigurationSource corsConfigurationSource(){
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(Arrays.asList("*"));
      configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
      configuration.setAllowedHeaders(Arrays.asList("*"));
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);

      return source;
   }



}
