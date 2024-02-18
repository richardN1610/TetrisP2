package com.tg.web_games.repository;

import com.tg.web_games.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserDetails,Long> {
        boolean existByEmailAddress(String email);

        @Query("SELECT u FROM UserDetails where u.emailAddress  = :email")
        Optional<UserDetails> findByEmailAddress(@Param("email") String email);
}
