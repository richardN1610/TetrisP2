package com.tg.web_games.repository;

import com.tg.web_games.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDetails,Long> {
        boolean existsByEmailAddress(String email);

        @Query("SELECT u FROM UserDetails u WHERE u.emailAddress  = :email")
        Optional<UserDetails> findByEmailAddress(@Param("email") String email);

        @Query("SELECT u FROM UserDetails u where LOWER(u.userName) = LOWER(:userName)")
        Optional<UserDetails> findByUserName(@Param("userName") String userName);

}
