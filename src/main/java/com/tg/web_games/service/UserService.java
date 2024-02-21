package com.tg.web_games.service;

import com.tg.web_games.dto.UserProfileDto;
import com.tg.web_games.dto.UserSignInDetailsDto;
import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserDetails;
import com.tg.web_games.exceptions.InvalidCredentialsException;
import com.tg.web_games.exceptions.UserNotFoundException;
import com.tg.web_games.mapper.SignUpMapper;
import com.tg.web_games.repository.UserRepository;
import com.tg.web_games.utils.InputValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.validator.EmailValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements InputValidator {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SignUpMapper userMapper;

    public String createUser(UserSignupDetails signupDetails){
        if (!isValidInput(signupDetails)) {
            return "Bad request";
        }
        boolean isUserExist = userRepository.existByEmailAddress(signupDetails.getEmailAddress());

        try{
            if(!isUserExist){
                UserDetails newUser = userMapper.signupDetails(signupDetails);
                newUser.setPasswordDetails(bCryptPasswordEncoder.encode(newUser.getPasswordDetails()));

                userRepository.save(newUser);
                return "User created";
            }
            return "User already exist.";
        }catch (Exception  e){
            return "Bad Request";
        }
    }

    public String signIn(UserSignInDetailsDto signInDetails, String password) throws InvalidCredentialsException {
        Optional<UserDetails> user = userRepository.findByEmailAddress(signInDetails.getEmailAddress());

        if(!user.isPresent()){
            throw new InvalidCredentialsException("Invalid Credentials.");
        }
        if(!bCryptPasswordEncoder.matches(password,user.get().getPasswordDetails())){
            throw new InvalidCredentialsException("Invalid Credentials.");
        }

        return "Logged in";
    }

    public void deleteUser(String emailAddress){
        Optional<UserDetails> user =userRepository.findByEmailAddress(emailAddress);

        if(user.isPresent()){
            UserDetails userToDelete = user.get();
            userRepository.deleteById(userToDelete.getUserId());
        }

    }

    public Boolean validateSignUpDetails(UserSignupDetails signupDetails) {
        EmailValidator validator = EmailValidator.getInstance();

        String regex = "^[\\p{L}\\p{Nd}'-]+(\\s[\\p{L}\\p{Nd}'-]+)*$";

        return signupDetails.getFirstName().matches(regex) && signupDetails.getLastName().matches(regex)
                && signupDetails.getUserName().matches(regex)
                &&validator.isValid(signupDetails.getEmailAddress());
    }

    public String updatePassword(String email,String newPassword) throws InvalidCredentialsException {
        Optional<UserDetails> user = userRepository.findByEmailAddress(email);

        if(!user.isPresent()){
            throw new InvalidCredentialsException("Invalid Credentials.");
        }

        if(newPassword.length() < 8){
            return "too short";
        }

        UserDetails validUser = user.get();
        validUser.setPasswordDetails(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(validUser);

        return "Password updated";
    }

    public UserProfileDto getUserProfileByUsername(String userName) throws UserNotFoundException {

        if(userName == null || userName.isEmpty()){
            throw new IllegalArgumentException("Invalid user name.");
        }

        UserDetails user = userRepository.findByUserName(userName)
                .orElseThrow(() ->new UserNotFoundException("User not found."));

        return userMapper.userProfile(user);
    }

    public Boolean validateSignInDetails(UserSignInDetailsDto signInDetails) {
        String regex = "^[\\p{L}\\p{Nd}'-]+(\\s[\\p{L}\\p{Nd}'-]+)*$";
        return signInDetails.getEmailAddress().matches(regex);
    }

    @Override
    public Boolean isValidInput(Object value) {
        if(value instanceof UserSignupDetails){
            return validateSignUpDetails((UserSignupDetails) value);
        }else if(value instanceof UserSignInDetailsDto){
            return validateSignInDetails((UserSignInDetailsDto) value);
        }else{
            throw new IllegalArgumentException("Unsupported input type.");
        }
    }
}
