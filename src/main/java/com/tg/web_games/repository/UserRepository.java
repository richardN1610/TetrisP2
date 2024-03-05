package com.tg.web_games.repository;

import com.tg.web_games.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserInfo,Long> {
        boolean existsByEmailAddress(String email);

        @Query("SELECT u FROM UserDetails u WHERE u.emailAddress  = :email")
        Optional<UserInfo> findByEmailAddress(@Param("email") String email);

        @Query("SELECT u FROM UserDetails u where LOWER(u.userName) = LOWER(:userName)")
        Optional<UserInfo> findByUserName(@Param("userName") String userName);

        @Query("UPDATE UserInfo u SET u.passwordDetails = :passwordDetails where u.userId = :userId")
        void updatePassword(@Param(value = "userId") Long userId,@Param(value="passwordDetails") String passwordDetails);

}
