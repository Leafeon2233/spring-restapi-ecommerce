package com.rene.ecommerce.services;

import com.rene.ecommerce.domain.dto.TypeDTO;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.repositories.ClientRepository;
import com.rene.ecommerce.security.ClientSS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ClientRepository clientRepo;

    @Autowired
    @InjectMocks
    private AuthService authService;


    @Test
    void sendNewPasswordTest() {
        String email = "zpu2@jhu.edu";
        Client dummyClient = new Client();
        dummyClient.setEmail(email);
        dummyClient.setPassword("123456");
        dummyClient.setId(100);
        when(clientRepo.findByEmail(email)).thenReturn(dummyClient);
        when(clientRepo.save(dummyClient)).thenReturn(dummyClient);
        authService.sendNewPassword(email);
        System.out.println(dummyClient.getPassword());
    }


    @Test
    void getTypeOfUser() {
        TypeDTO type;
        ClientSS clientSS = new ClientSS();
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);

            type = authService.getTypeOfUser();
            System.out.println(type.getType());
            assertEquals("Client",type.getType());
        } catch (Exception e){
            System.out.println("wdnmd wtf is wrong");
        }

    }
}