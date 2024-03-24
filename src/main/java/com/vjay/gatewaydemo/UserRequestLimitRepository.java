package com.vjay.gatewaydemo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRequestLimitRepository extends JpaRepository<UserRequestRate, Long> {

    Optional<UserRequestRate> findByUser(AppUser user);
}
