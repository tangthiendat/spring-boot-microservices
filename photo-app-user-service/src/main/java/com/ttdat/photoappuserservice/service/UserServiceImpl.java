package com.ttdat.photoappuserservice.service;

import com.ttdat.photoappuserservice.UserMapper;
import com.ttdat.photoappuserservice.dto.*;
import com.ttdat.photoappuserservice.entity.User;
import com.ttdat.photoappuserservice.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserMapper userMapper;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthenticationManager authenticationManager;
    JwtService jwtService;
    UserDetailsService userDetailsService;

    @Override
    public UserDTO createUser(UserDTO userDetails) {
        User user = userMapper.toUser(userDetails);
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        return userMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String accessToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        final String userEmail = jwtService.extractUsername(request.getToken());
        if (userEmail != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if(jwtService.isTokenValid(request.getToken(), userDetails)) {
                return IntrospectResponse.builder()
                        .valid(true)
                        .build();
            }
        }
        return IntrospectResponse.builder()
                .valid(false)
                .build();
    }
}
