package com.rene.ecommerce.services;

import com.rene.ecommerce.domain.dto.TypeDTO;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.exceptions.ObjectNotFoundException;
import com.rene.ecommerce.repositories.ClientRepository;
import com.rene.ecommerce.repositories.SellerRepository;
import com.rene.ecommerce.security.ClientSS;
import com.rene.ecommerce.security.SellerSS;
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

    @Mock
    private SellerRepository sellerRepo;

    @Autowired
    @InjectMocks
    private AuthService authService;


    @Test
    void sendNewPasswordTestClientSuccess() {
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
    void sendNewPasswordTestSellerSuccess() {
        String email = "dummy@gmail.com";
        when(clientRepo.findByEmail(email)).thenReturn(null);
        Seller dummySeller = new Seller();
        dummySeller.setEmail(email);
        dummySeller.setPassword("123456");
        when(sellerRepo.findByEmail(email)).thenReturn(dummySeller);
        authService.sendNewPassword(email);
    }

    @Test
    void sendNewPasswordTestUserNotFound() {
        String email = "dummy@gmail.com";
        when(clientRepo.findByEmail(email)).thenReturn(null);
        Seller dummySeller = new Seller();
        dummySeller.setEmail(email);
        dummySeller.setPassword("123456");
        when(sellerRepo.findByEmail(email)).thenReturn(null);
        assertThrows(ObjectNotFoundException.class, () -> {
            authService.sendNewPassword(email);
        });
    }


    @Test
    void getTypeOfUserClient() {
        TypeDTO type;
        ClientSS clientSS = new ClientSS();
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        userService.when(UserService::clientAuthenticated).thenReturn(clientSS);

        type = authService.getTypeOfUser();
        System.out.println(type.getType());
        assertEquals("Client", type.getType());
        userService.close();
    }

    @Test
    void getTypeOfUserSeller() {
        TypeDTO type;
        SellerSS sellerSS = new SellerSS();
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);

        type = authService.getTypeOfUser();
        System.out.println(type.getType());
        assertEquals("Seller", type.getType());
        userService.close();

    }
}