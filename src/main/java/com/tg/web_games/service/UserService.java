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
import com.tg.web_games.utils.InputValidator;
import com.tg.web_games.utils.Roles;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SignUpMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                return userRepository.findByEmailAddress(email).orElseThrow(() -> new EntityNotFoundException("Entity not found"));
            }
        };
    }

    public AuthenticationResponse createUser(UserSignupDetails signupDetails) throws DuplicateUserException, InvalidCredentialsException {
        if (!validateSignUpDetails(signupDetails)) {
            throw new InvalidCredentialsException("Invalid request");
        }
        boolean isEmailTaken = userRepository.existsByEmailAddress(signupDetails.getEmailAddress());

        if(isEmailTaken){
            throw new DuplicateUserException("Email address is already taken");
        }

        UserInfo newUser = userMapper.signupDetails(signupDetails);
        newUser.setPasswordDetails(bCryptPasswordEncoder.encode(newUser.getPasswordDetails()));
        newUser.setUserRole(Roles.USER_PLAYER);
        userRepository.save(newUser);
        var jwtToken = jwtService.generateToken(newUser);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
    public AuthenticationResponse signIn(UserSignInDetailsDto signInDetails, String password) throws InvalidCredentialsException, UserNotFoundException {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInDetails.getEmailAddress(),password
                )
        );

        var user = userRepository.findByEmailAddress(signInDetails.getEmailAddress())
                .orElseThrow(() -> new UserNotFoundException("Invalid user"));
        var jwtToken =jwtService.generateToken(user);

        if(!bCryptPasswordEncoder.matches(password,user.getPasswordDetails())){
            throw new InvalidCredentialsException("Invalid Credentials.");
        }

        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    public void deleteUser(String emailAddress) throws UserNotFoundException, UnauthorizedOperationException {
        Optional<UserInfo> user = userRepository.findByEmailAddress(emailAddress);

        if (!user.isPresent()) {
            throw new UserNotFoundException("User with email address '" + emailAddress + "' not found.");
        }

        UserInfo userToDelete = user.get();

        if (!isUserAuthorized(userToDelete.getEmailAddress())) {
            throw new UnauthorizedOperationException("You are not authorized to delete this user.");
        }

        userRepository.deleteById(userToDelete.getUserId());
    }

    public AuthenticationResponse updatePassword(String email,String newPassword) throws InvalidCredentialsException {
        Optional<UserInfo> user = userRepository.findByEmailAddress(email);

        if(!user.isPresent()){
            throw new InvalidCredentialsException("Invalid Credentials.");
        }

        if(newPassword.length() < 8){
            throw new IllegalArgumentException("Password is too short.");
        }

        if(!isUserAuthorized(email)){

            return AuthenticationResponse.builder().token("Unauthorized request.").build();
        }
        UserInfo validUser = user.get();
        String encodedPw = bCryptPasswordEncoder.encode(newPassword);
        userRepository.updatePassword(validUser.getUserId(),encodedPw);
        var jwtToken =jwtService.generateToken(validUser);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }

    private Boolean validateSignUpDetails(@NotNull UserSignupDetails signupDetails) {
        EmailValidator validator = EmailValidator.getInstance();

        String nameRegex = "^[a-zA-Z]+(?: [a-zA-Z]+)*$";
        String userNameRegex = "^[a-zA-Z0-9]+(?: [a-zA-Z0-9]+)*$";
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\\$%^&*])[a-zA-Z0-9!@#\\$%^&*]{8,}$";

        return signupDetails.getFirstName().matches(nameRegex) && signupDetails.getLastName().matches(nameRegex)
                && signupDetails.getUserName().matches(userNameRegex)
                && signupDetails.getPasswordDetails().matches(passwordRegex)
                &&validator.isValid(signupDetails.getEmailAddress());
    }


    public UserProfileDto getUserProfileByUsername(String userName) throws UserNotFoundException {

        if(userName == null || userName.isEmpty()){
            throw new IllegalArgumentException("Invalid user name.");
        }

        UserInfo user = userRepository.findByUserName(userName)
                .orElseThrow(() ->new UserNotFoundException("User not found."));

        return userMapper.userProfile(user);
    }

    private Boolean validateSignInDetails(UserSignInDetailsDto signInDetails) {
        String regex = "^[\\p{L}\\p{Nd}'-]+(\\s[\\p{L}\\p{Nd}'-]+)*$";
        return signInDetails.getEmailAddress().matches(regex);
    }

    private Boolean isUserAuthorized(String userEmail){
        UserInfo validUser = (UserInfo)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return validUser.getEmailAddress().equals(userEmail);
    }
}
