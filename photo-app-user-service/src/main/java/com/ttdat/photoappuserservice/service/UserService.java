package com.ttdat.photoappuserservice.service;


import com.ttdat.photoappuserservice.dto.*;

public interface UserService {
    UserDTO createUser(UserDTO userDetails);
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    IntrospectResponse introspect(IntrospectRequest introspectRequest);
    boolean isTokenValid(String jwt);
}
