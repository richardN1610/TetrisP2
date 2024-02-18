package com.tg.web_games.service;

import com.tg.web_games.dto.UserSignInDetails;
import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserDetails;
import com.tg.web_games.mapper.SignUpMapper;
import com.tg.web_games.repository.UserRepository;
import com.tg.web_games.utils.InputValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.commons.validator.EmailValidator;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements InputValidator {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SignUpMapper signUpMapper;

    public String createUser(UserSignupDetails signupDetails){
        if (!isValidInput(signupDetails)) {
            return "Bad request";
        }

        signupDetails.setPasswordDetails(bCryptPasswordEncoder.encode(signupDetails.getPasswordDetails()));

        try{
            UserDetails newUser = signUpMapper.signupDetails(signupDetails);
            userRepository.save(newUser);
            return "User created";
        }catch (Exception  e){
            return "Bad Request";
        }
    }

    public String signIn(UserSignInDetails signInDetails){
        Optional<UserDetails> user = userRepository.findByEmailAddress(signInDetails.getEmailAddress());

        if(user.isPresent()){
            if(user.get().getPasswordDetails().equals(signInDetails.getPasswordDetails())){
                return "Signed in successfully";
            }
        }

        return "Invalid user";
    }


    public void deleteUser(String emailAddress){
        Optional<UserDetails> user = userRepository.findByEmailAddress(emailAddress);

        userRepository.deleteById(user.get().getUserId());
    }

    public Boolean validateSignUpDetails(UserSignupDetails signupDetails) {
        EmailValidator validator = EmailValidator.getInstance();

        String regex = "^[\\p{L}\\p{Nd}'-]+(\\s[\\p{L}\\p{Nd}'-]+)*$";

        return signupDetails.getFirstName().matches(regex) && signupDetails.getLastName().matches(regex)
                && signupDetails.getUserName().matches(regex)
                &&validator.isValid(signupDetails.getEmailAddress());
    }


    public Boolean validateSignInDetails(UserSignInDetails signInDetails) {
        String regex = "^[\\p{L}\\p{Nd}'-]+(\\s[\\p{L}\\p{Nd}'-]+)*$";
        return signInDetails.getEmailAddress().matches(regex);
    }

    @Override
    public Boolean isValidInput(Object value) {
        if(value instanceof UserSignupDetails){
            return validateSignUpDetails((UserSignupDetails) value);
        }else if(value instanceof  UserSignInDetails){
            return validateSignInDetails((UserSignInDetails) value);
        }else{
            throw new IllegalArgumentException("Unsupported input type.");
        }
    }
}
