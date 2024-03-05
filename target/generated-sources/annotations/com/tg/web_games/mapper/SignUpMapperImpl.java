package com.tg.web_games.mapper;

import com.tg.web_games.dto.UserProfileDto;
import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserInfo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-25T18:36:55+1100",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.1 (Oracle Corporation)"
)
@Component
public class SignUpMapperImpl implements SignUpMapper {

    @Override
    public UserInfo signupDetails(UserSignupDetails userSignupDetails) {
        if ( userSignupDetails == null ) {
            return null;
        }

        UserInfo.UserInfoBuilder userInfo = UserInfo.builder();

        userInfo.firstName( userSignupDetails.getFirstName() );
        userInfo.lastName( userSignupDetails.getLastName() );
        userInfo.userName( userSignupDetails.getUserName() );
        userInfo.emailAddress( userSignupDetails.getEmailAddress() );
        userInfo.passwordDetails( userSignupDetails.getPasswordDetails() );

        return userInfo.build();
    }

    @Override
    public UserProfileDto userProfile(UserInfo userDetails) {
        if ( userDetails == null ) {
            return null;
        }

        UserProfileDto.UserProfileDtoBuilder userProfileDto = UserProfileDto.builder();

        return userProfileDto.build();
    }
}
