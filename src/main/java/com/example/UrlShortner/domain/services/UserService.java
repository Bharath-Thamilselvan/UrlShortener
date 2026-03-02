package com.example.UrlShortner.domain.services;

import com.example.UrlShortner.domain.entities.User;
import com.example.UrlShortner.domain.models.CreateUserCmd;
import com.example.UrlShortner.domain.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @Transactional
    public void createUser(CreateUserCmd cmd) {
        if(userRepository.existsByEmail(cmd.email())){
            throw new RuntimeException("Email alreaddy exists");
        }
        User user=new User();
        user.setEmail(cmd.email());
        user.setPassword(passwordEncoder.encode(cmd.password()));
        user.setName(cmd.name());
        user.setRole(cmd.role());
        user.setCreatedAt(Instant.now());
        userRepository.save(user);
    }
}
