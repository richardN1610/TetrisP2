package com.tg.web_games.mapper;

import com.tg.web_games.dto.UserProfileDto;
import com.tg.web_games.dto.UserSignupDetails;
import com.tg.web_games.entity.UserDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper (componentModel = "spring")
public interface SignUpMapper {
    SignUpMapper INSTANCE = Mappers.getMapper(SignUpMapper.class);

    @Mapping(target = "userId", ignore = true)
    UserDetails signupDetails(UserSignupDetails userSignupDetails);

    UserProfileDto userProfile(UserDetails userDetails);
}
