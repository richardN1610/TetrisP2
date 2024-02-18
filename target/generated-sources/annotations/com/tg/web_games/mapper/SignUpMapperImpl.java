package com.tg.web_games.mapper;

import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserDetails;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-17T20:58:24+1100",
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
}
