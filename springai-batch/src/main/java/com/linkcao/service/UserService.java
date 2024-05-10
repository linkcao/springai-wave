package com.linkcao.service;

import com.linkcao.entity.Users;
import com.linkcao.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UsersRepository usersRepository;

    public Users findByUsername(String username){
        Users user = usersRepository.findByUsername(username);
        return user;
    }

}
