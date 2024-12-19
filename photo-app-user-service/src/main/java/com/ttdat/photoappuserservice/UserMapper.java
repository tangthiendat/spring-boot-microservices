package com.ttdat.photoappuserservice;

import com.ttdat.photoappuserservice.dto.UserDTO;
import com.ttdat.photoappuserservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toUserDTO(User user);
    User toUser(UserDTO userDto);
}
