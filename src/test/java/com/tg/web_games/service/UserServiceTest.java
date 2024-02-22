package com.tg.web_games.service;

import com.tg.web_games.dto.UserProfileDto;
import com.tg.web_games.dto.UserSignInDetailsDto;
import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserDetails;
import com.tg.web_games.exceptions.InvalidCredentialsException;
import com.tg.web_games.exceptions.UserNotFoundException;
import com.tg.web_games.mapper.SignUpMapper;
import com.tg.web_games.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    @InjectMocks
    UserService userService;

    UserSignupDetails signupDetails;
    UserDetails user;

    @BeforeEach
    public void setup(){
        signupDetails = new UserSignupDetails("Bobby","Bob","DaBob123","BobbyBob123@gmail.com","password123");
        user = new UserDetails(1L, "Bobby", "Bob", "DaBob123", "BobbyBob123@gmail.com", "password123");

    }

    @Test
    void createUser_if_email_not_taken() {
        //mocking bcrypt
        when(userRepository.existsByEmailAddress(signupDetails.getEmailAddress())).thenReturn(false);

        //mocking user
        UserDetails user = new UserDetails(1L,signupDetails.getFirstName(),signupDetails.getLastName(),signupDetails.getUserName(),signupDetails.getEmailAddress(), signupDetails.getPasswordDetails());
        when(userMapper.signupDetails(signupDetails)).thenReturn(user);
        when(passwordEncoder.encode(user.getPasswordDetails())).thenReturn("SecretPw");
        when(userRepository.save(any(UserDetails.class))).thenReturn(user);

        //To change to a different response type later.
        String actualResponse = userService.createUser(signupDetails);

        //asserting expect behavior
        assertEquals("User created",actualResponse);
        assertEquals("SecretPw",user.getPasswordDetails());
        assertEquals("Bobby",user.getFirstName());
        assertEquals("Bob",user.getLastName());
        assertEquals("BobbyBob123@gmail.com",user.getEmailAddress());

        verify(passwordEncoder,times(1)).encode("password123");
        verify(userRepository,times(1)).existsByEmailAddress("BobbyBob123@gmail.com");
        verify(userMapper,times(1)).signupDetails(signupDetails);
        verify(userRepository,times(1)).save(user);
    }

    @Test
    void create_user_when_email_is_taken(){
        when(userRepository.existsByEmailAddress(signupDetails.getEmailAddress())).thenReturn(true);

        String expectedMsg = "User already exist.";
        String actualMsg = userService.createUser(signupDetails);

        assertEquals(expectedMsg,actualMsg);

        verify(userRepository,times(1)).existsByEmailAddress("BobbyBob123@gmail.com");
        verify(userRepository,never()).save(any(UserDetails.class));
    }

    @Test
    void signIn_successfully() throws InvalidCredentialsException {
        UserSignInDetailsDto signInDetails = new UserSignInDetailsDto("test@gmail.com");
        String password = "password123";

        when(userRepository.findByEmailAddress(signInDetails.getEmailAddress())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123",user.getPasswordDetails())).thenReturn(true);

        String expectedMsg = userService.signIn(signInDetails,password);

        verify(userRepository,times(1)).findByEmailAddress("test@gmail.com");
        verify(passwordEncoder,times(1)).matches("password123",user.getPasswordDetails());

        assertEquals("Logged in",expectedMsg);
    }

    @Test
    void signIn_Invalid_Email() throws InvalidCredentialsException {
        UserSignInDetailsDto signInDetails = new UserSignInDetailsDto("fakeemail@gmail.com");

        when(userRepository.findByEmailAddress(signInDetails.getEmailAddress())).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.signIn(signInDetails, "abc123123");
        }, "Invalid Credentials.");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void  signIn_invalid_password() throws InvalidCredentialsException {
        UserSignInDetailsDto signInDetails = new UserSignInDetailsDto("test@gmail.com");
        String invalidPassword = "wrongPassword123"; // Different from stored password

        // Mock user repository (optional)
        when(userRepository.findByEmailAddress(signInDetails.getEmailAddress())).thenReturn(Optional.of(user));

        // Act and Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.signIn(signInDetails, invalidPassword);
        }, "Invalid Credentials."); // Check for password related message

        //mock verification
        verify(passwordEncoder, times(1)).matches(invalidPassword, user.getPasswordDetails());
    }

    @Test
    void deleteUser() {
        when(userRepository.findByEmailAddress("BobbyBob123@gmail.com")).thenReturn(Optional.of(user));

        userService.deleteUser(user.getEmailAddress());

        verify(userRepository,times(1)).findByEmailAddress(user.getEmailAddress());
        verify(userRepository,times(1)).deleteById(1L);
    }

    @Test
    void validateSignUpDetails_valid_details() {
        boolean isValidDetails = userService.validateSignUpDetails(signupDetails);
        assertEquals(true,isValidDetails);
    }

    @Test
    void validateSignUpDetails_invalid_firstname_details() {
        signupDetails.setFirstName("Bobby Smith X!Z");
        boolean isValidDetails = userService.validateSignUpDetails(signupDetails);
        assertEquals(false,isValidDetails);
    }
    @Test
    void validateSignUpDetails_invalid_lastname_details() {
        signupDetails.setLastName("No Work "); //cannot end with space
        boolean isValidDetails = userService.validateSignUpDetails(signupDetails);
        assertEquals(false,isValidDetails);
    }
    @Test
    void validateSignUpDetails_invalid_username_details() {
        signupDetails.setFirstName("&@");
        boolean isValidDetails = userService.validateSignUpDetails(signupDetails);
        assertEquals(false,isValidDetails);
    }

    @Test
    void updatePassword_successfully() throws InvalidCredentialsException {
        String oldw = user.getPasswordDetails();
        String newPw = "newUserPw#213";

        when(userRepository.findByEmailAddress(user.getEmailAddress())).thenReturn(Optional.ofNullable(user));
        when(passwordEncoder.encode(newPw)).thenReturn("SecretPw");
        when(userRepository.save(user)).thenReturn(user);

        String expectedMsg = "Password updated";
        String actualMsg = userService.updatePassword(user.getEmailAddress(),newPw);

        assertEquals(expectedMsg,actualMsg);
        assertNotEquals(oldw,user.getPasswordDetails());
        assertEquals("SecretPw",user.getPasswordDetails());

        verify(userRepository, times(1)).findByEmailAddress(user.getEmailAddress());
        verify(passwordEncoder,times(1)).encode(newPw);
        verify(userRepository,times(1)).save(user);
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