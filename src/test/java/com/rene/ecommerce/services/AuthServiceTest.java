package com.rene.ecommerce.services;

import com.rene.ecommerce.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void sendNewPasswordTest() {
        String email = "zpu2@jhu.edu";

        authService.sendNewPassword(email);


    }

    @Test
    void getTypeOfUser() {
    }
}