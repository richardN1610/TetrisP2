package com.tg.web_games.repository;

import com.tg.web_games.dto.UserProfileDto;
import com.tg.web_games.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDetails,Long> {
        boolean existByEmailAddress(String email);

        @Query("SELECT u FROM UserDetails where u.emailAddress  = :email")
        Optional<UserDetails> findByEmailAddress(@Param("email") String email);

        @Query("SELECT u FROM UserDetails where LOWER(u.userName) = LOWER(:userName)")
        Optional<UserDetails> findByUserName(@Param("userName") String userName);

}
