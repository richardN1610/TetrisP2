package com.tg.web_games.mapper;

import com.tg.web_games.dto.UserProfileDto;
import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserDetails;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-22T19:25:27+1100",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class SignUpMapperImpl implements SignUpMapper {

    @Override
    public UserDetails signupDetails(UserSignupDetails userSignupDetails) {
        if ( userSignupDetails == null ) {
            return null;
        }

        UserDetails.UserDetailsBuilder userDetails = UserDetails.builder();

        userDetails.firstName( userSignupDetails.getFirstName() );
        userDetails.lastName( userSignupDetails.getLastName() );
        userDetails.userName( userSignupDetails.getUserName() );
        userDetails.emailAddress( userSignupDetails.getEmailAddress() );
        userDetails.passwordDetails( userSignupDetails.getPasswordDetails() );

        return userDetails.build();
    }

    @Override
    public UserProfileDto userProfile(UserDetails userDetails) {
        if ( userDetails == null ) {
            return null;
        }

        UserProfileDto.UserProfileDtoBuilder userProfileDto = UserProfileDto.builder();

        userProfileDto.userName( userDetails.getUserName() );

        return userProfileDto.build();
    }
}
