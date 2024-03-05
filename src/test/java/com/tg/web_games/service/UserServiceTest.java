package com.tg.web_games.service;

import com.tg.web_games.config.JwtService;
import com.tg.web_games.dto.UserProfileDto;
import com.tg.web_games.dto.UserSignInDetailsDto;
import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserInfo;
import com.tg.web_games.exceptions.DuplicateUserException;
import com.tg.web_games.exceptions.InvalidCredentialsException;
import com.tg.web_games.exceptions.UnauthorizedOperationException;
import com.tg.web_games.exceptions.UserNotFoundException;
import com.tg.web_games.mapper.SignUpMapper;
import com.tg.web_games.repository.UserRepository;
import com.tg.web_games.utils.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    SignUpMapper userMapper;
    @Mock
    BCryptPasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @Mock
    AuthenticationManager authManager;
    @InjectMocks
    UserService userService;

    UserSignupDetails signupDetails;
    UserInfo user;

    @BeforeEach
    public void setup(){
        signupDetails = new UserSignupDetails("Bobby","Bob","DaBob123","BobbyBob123@gmail.com","Pa!!asW342w");
        user = UserInfo.builder()
                .userId(1L)
                .firstName("Bobby")
                .lastName("Bob")
                .userName("DaBob123")
                .emailAddress("BobbyBob123@gmail.com")
                .passwordDetails("Pa!!asW342w")
                .userRole(Roles.USER_PLAYER)
                .build();
    }

    @Test
    void createUser_if_email_not_taken() throws Exception {
        when(userRepository.existsByEmailAddress(signupDetails.getEmailAddress())).thenReturn(false);
        when(userMapper.signupDetails(signupDetails)).thenReturn(user);
        when(passwordEncoder.encode(user.getPasswordDetails())).thenReturn("SecretPw");
        when(userRepository.save(any(UserInfo.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("secrettoken");

        AuthenticationResponse actualResponse = userService.createUser(signupDetails);
        String expectResult = "secrettoken";

        assertEquals("SecretPw",user.getPasswordDetails());
        assertEquals("Bobby",user.getFirstName());
        assertEquals("Bob",user.getLastName());
        assertEquals("BobbyBob123@gmail.com",user.getEmailAddress());
        assertEquals(expectResult,actualResponse.getToken());

        verify(userRepository,times(1)).existsByEmailAddress("BobbyBob123@gmail.com");
        verify(userMapper,times(1)).signupDetails(signupDetails);
        verify(userRepository,times(1)).save(user);
        verify(jwtService,times(1)).generateToken(user);
    }

    @Test
    void updatePassword_successfully() throws InvalidCredentialsException {
        String newPassword = "newUserPw#213";

        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));
        when(userRepository.findByEmailAddress(user.getEmailAddress())).thenReturn(Optional.of(user));
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

        when(passwordEncoder.encode(newPassword)).thenReturn("dummyEncodedPassword");
        when(jwtService.generateToken(user)).thenReturn("secrettoken");

        AuthenticationResponse response = userService.updatePassword(user.getEmailAddress(), newPassword);

        assertEquals("secrettoken",response.getToken());

        verify(userRepository, times(1)).findByEmailAddress(user.getEmailAddress());
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).updatePassword(anyLong(), eq("dummyEncodedPassword"));
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void create_user_when_email_is_taken() throws Exception {

        when(userRepository.existsByEmailAddress(signupDetails.getEmailAddress())).thenReturn(true);

        assertThrows(DuplicateUserException.class, () ->{
            userService.createUser(signupDetails);
        });

        verify(userRepository,times(1)).existsByEmailAddress("BobbyBob123@gmail.com");
        verify(userRepository,never()).save(any(UserInfo.class));
    }

    @Test
    void when_signup_email_format_not_valid(){
        String invalidEmailFormat = "Thisisavalidfor@mat@gmail.com";
        signupDetails.setEmailAddress(invalidEmailFormat);
        assertThrows(InvalidCredentialsException.class, () ->{
            userService.createUser(signupDetails);
        });
    }

    @Test
    void signIn_successfully() throws InvalidCredentialsException, UserNotFoundException {
        UserSignInDetailsDto signInDetails = new UserSignInDetailsDto("test@gmail.com");
        String password = "password123";

        when(userRepository.findByEmailAddress(signInDetails.getEmailAddress())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123",user.getPasswordDetails())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("secrettoken");

        AuthenticationResponse expectedToken = userService.signIn(signInDetails,"password123");

        verify(userRepository,times(1)).findByEmailAddress("test@gmail.com");
        verify(passwordEncoder,times(1)).matches("password123",user.getPasswordDetails());

        assertEquals("secrettoken",expectedToken.getToken());
    }

    @Test
    void signIn_Invalid_Email() throws InvalidCredentialsException {
        UserSignInDetailsDto signInDetails = new UserSignInDetailsDto("fakeemail@gmail.com");

        when(userRepository.findByEmailAddress(signInDetails.getEmailAddress())).thenReturn(Optional.empty());
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(signInDetails.getEmailAddress(), null, Collections.emptyList()));
        // Act and Assert
        assertThrows(UserNotFoundException.class, () -> {
            userService.signIn(signInDetails, "abc123123");
        }, "Invalid Credentials.");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void  signIn_invalid_password() throws InvalidCredentialsException {
        UserSignInDetailsDto signInDetails = new UserSignInDetailsDto("test@gmail.com");
        String invalidPassword = "wrongPassword123"; // Different from stored password

        when(userRepository.findByEmailAddress(signInDetails.getEmailAddress())).thenReturn(Optional.of(user));
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(signInDetails.getEmailAddress(),invalidPassword));

        assertThrows(InvalidCredentialsException.class, () -> {
            userService.signIn(signInDetails, invalidPassword);
        }, "Invalid Credentials."); // Check for password related message

        verify(passwordEncoder, times(1)).matches(invalidPassword, user.getPasswordDetails());
    }

    @Test
    void delete_user_if_same_email_address() throws UserNotFoundException, UnauthorizedOperationException {

        String userEmail = "BobbyBob123@gmail.com";

        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));
        when(userRepository.findByEmailAddress(user.getEmailAddress())).thenReturn(Optional.of(user));
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

        userService.deleteUser(userEmail);

        verify(userRepository, times(1)).findByEmailAddress(user.getEmailAddress());
        verify(userRepository, times(1)).deleteById(user.getUserId());
    }

    @Test
    void cannot_delete_user_if_different_email_address() throws UserNotFoundException {

        String invalidEmail = "DaBobby1231@gmail.com";

        SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(user);

        UserInfo userToFind = new UserInfo();
        userToFind.setUserId(2L);
        userToFind.setEmailAddress(invalidEmail);
        when(userRepository.findByEmailAddress(invalidEmail)).thenReturn(Optional.of(userToFind));

        assertThrows(UnauthorizedOperationException.class, () -> {
            userService.deleteUser(invalidEmail);
        });

        // Verify no interactions with the repository for deletion
        verify(userRepository, times(0)).deleteById(anyLong());
    }


    @Test
    void search_user_by_valid_username() throws UserNotFoundException {
        UserProfileDto expectedUserProfile = new UserProfileDto("DaBob123");

        when(userRepository.findByUserName(expectedUserProfile.getUserName())).thenReturn(Optional.of(user));
        when(userMapper.userProfile(user)).thenReturn(expectedUserProfile);

        UserProfileDto actualUserProfile = userService.getUserProfileByUsername(expectedUserProfile.getUserName());

        assertEquals(actualUserProfile.getUserName(),expectedUserProfile.getUserName());

        verify(userRepository,times(1)).findByUserName("DaBob123");
        verify(userMapper,times(1)).userProfile(user);
    }

    @Test
    void search_user_by_invalid_username() throws UserNotFoundException {
        UserProfileDto unexpectedUserProfile = new UserProfileDto("DaBob1235");

        when(userRepository.findByUserName(unexpectedUserProfile.getUserName())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->{
            userService.getUserProfileByUsername(unexpectedUserProfile.getUserName());
        });

        verify(userRepository,times(1)).findByUserName("DaBob1235");
    }

}