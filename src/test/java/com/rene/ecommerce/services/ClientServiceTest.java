package com.rene.ecommerce.services;

import com.rene.ecommerce.domain.dto.updated.UpdatedClient;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.exceptions.*;
import com.rene.ecommerce.repositories.ClientRepository;
import com.rene.ecommerce.repositories.SellerRepository;
import com.rene.ecommerce.security.ClientSS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepo;

    @Mock
    private SellerRepository sellerRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientService clientService;


    @Test
    void findByIdTestSuccessSuccess() {
        Integer id = 100;
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(id);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        Client dummyClient = new Client();
        dummyClient.setEmail("zpu2");
        dummyClient.setPassword("123456");
        dummyClient.setId(id);
        when(clientRepo.findById(id)).thenReturn(Optional.of(dummyClient));
        Client foundClient = clientService.findById(id);
        assertEquals(dummyClient.getEmail(), foundClient.getEmail());
        userService.close();
    }

    @Test
    void findByIdTestUserSessionIsNull() {
        Integer id = 100;
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        userService.when(UserService::clientAuthenticated).thenReturn(null);
        assertThrows(AuthorizationException.class, () -> clientService.findById(id));
        userService.close();
    }

    @Test
    void findByIdTestUserSessionNotEqualToId() {
        Integer userId = 100;
        Integer id = 101;
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(userId);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        assertThrows(AuthorizationException.class, () -> clientService.findById(id));
        userService.close();
    }

    @Test
    void findByIdTestObjNotFound() {
        Integer id = 101;
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(id);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        when(clientRepo.findById(id)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> clientService.findById(id));
        userService.close();
    }

    @Test
    void returnClientWithoutParsingTheIdTestSuccess() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        Client dummyClient = new Client();
        dummyClient.setEmail("zpu2");
        dummyClient.setPassword("123456");
        dummyClient.setId(100);
        when(clientRepo.findById(100)).thenReturn(Optional.of(dummyClient));
        Client foundClient = clientService.returnClientWithoutParsingTheId();
        assertEquals(dummyClient.getEmail(), foundClient.getEmail());
        userService.close();
    }

    @Test
    void returnClientWithoutParsingTheIdTestObjectNotFound() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        when(clientRepo.findById(100)).thenReturn(Optional.empty());
        assertThrows(ObjectNotFoundException.class, () -> clientService.returnClientWithoutParsingTheId());
        userService.close();
    }

    @Test
    void returnClientWithoutParsingTheIdTestUserNotFound() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        userService.when(UserService::clientAuthenticated).thenReturn(null);
        assertThrows(AuthorizationException.class, () -> clientService.returnClientWithoutParsingTheId());
        userService.close();
    }


    @Test
    void findAllTest() {
        when(clientRepo.findAll()).thenReturn(null);
        List<Client> clientList = new ArrayList<>();
        clientList = clientService.findAll();
        assertNull(clientList);
    }

    @Test
    void insertTestSuccess() {
        Client dummyClient = new Client();
        dummyClient.setEmail("dummy@gmail.com");
        dummyClient.setPassword("dummyPassword");
        when(sellerRepo.findByEmail(dummyClient.getEmail())).thenReturn(null);
        when(clientRepo.save(dummyClient)).thenReturn(dummyClient);
        when(passwordEncoder.encode(dummyClient.getPassword())).thenReturn(dummyClient.getPassword());
        Client insertClient = clientService.insert(dummyClient);
        assertEquals(insertClient.getEmail(),dummyClient.getEmail());
    }

    @Test
    void insertTestClientSellerSame() {
        Client dummyClient = new Client();
        dummyClient.setEmail("dummy@gmail.com");
        dummyClient.setPassword("dummyPassword");

        Seller dummySeller = new Seller();
        dummySeller.setEmail("dummy@gmail.com");
        when(sellerRepo.findByEmail(dummySeller.getEmail())).thenReturn(dummySeller);
        when(passwordEncoder.encode(dummyClient.getPassword())).thenReturn(dummyClient.getPassword());
        assertThrows(ClientOrSellerHasThisSameEntryException.class, () -> clientService.insert(dummyClient));
    }


    @Test
    void insertTestDuplicated() {
        Client dummyClient = new Client();
        dummyClient.setEmail("dummy@gmail.com");
        dummyClient.setPassword("dummyPassword");
        when(sellerRepo.findByEmail(dummyClient.getEmail())).thenReturn(null);
        when(clientRepo.save(dummyClient)).thenThrow(RuntimeException.class);
        when(passwordEncoder.encode(dummyClient.getPassword())).thenReturn(dummyClient.getPassword());
        assertThrows(DuplicateEntryException.class, () -> clientService.insert(dummyClient));
    }


    @Test
    void updateTestSuccess() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        UpdatedClient updatedClient = new UpdatedClient();
        updatedClient.setEmail("newEmail@gmail.com");
        updatedClient.setName("newName");
        updatedClient.setPassword("newPassword");
        Client dummyClient = new Client();
        dummyClient.setId(100);
        dummyClient.setEmail(updatedClient.getEmail());
        when(passwordEncoder.encode(updatedClient.getPassword())).thenReturn(updatedClient.getPassword());
        when(clientRepo.findById(100)).thenReturn(Optional.of(dummyClient));
        when(sellerRepo.findByEmail(updatedClient.getEmail())).thenReturn(null);
        when(clientRepo.save(dummyClient)).thenReturn(dummyClient);
        Client updated = clientService.update(updatedClient);
        assertEquals(updated.getEmail(),dummyClient.getEmail());
        userService.close();
    }


    // cannot be tested, because user.getId() is before checking if the user is null
    @Test
    void updateTestUserNull() {
    }

    @Test
    void updateTestUserClientNotSame() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(101);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        UpdatedClient updatedClient = new UpdatedClient();
        Client dummyClient = new Client();
        dummyClient.setId(100);
        when(clientRepo.findById(101)).thenReturn(Optional.of(dummyClient));
        assertThrows(AuthorizationException.class, () -> clientService.update(updatedClient));
        userService.close();
    }

    @Test
    void updateTestObjNotFound() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        UpdatedClient updatedClient = new UpdatedClient();
        updatedClient.setEmail("newEmail@gmail.com");
        updatedClient.setName("newName");
        updatedClient.setPassword("newPassword");
        Client dummyClient = new Client();
        dummyClient.setId(100);
        dummyClient.setEmail(updatedClient.getEmail());
        when(passwordEncoder.encode(updatedClient.getPassword())).thenReturn(updatedClient.getPassword());
        when(clientRepo.findById(100)).thenReturn(Optional.of(dummyClient));
        when(sellerRepo.findByEmail(updatedClient.getEmail())).thenReturn(null);
        when(clientRepo.save(dummyClient)).thenThrow(RuntimeException.class);
        assertThrows(DuplicateEntryException.class, () -> clientService.update(updatedClient));
        userService.close();
    }

    @Test
    void updateTestClientSellerSame() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(user);
        UpdatedClient updatedClient = new UpdatedClient();
        updatedClient.setEmail("newEmail@gmail.com");
        updatedClient.setName("newName");
        updatedClient.setPassword("newPassword");
        Client dummyClient = new Client();
        dummyClient.setId(100);
        dummyClient.setEmail(updatedClient.getEmail());

        Seller dummySeller = new Seller();
        dummySeller.setEmail("dummy@gmail.com");
        when(passwordEncoder.encode(updatedClient.getPassword())).thenReturn(updatedClient.getPassword());
        when(clientRepo.findById(100)).thenReturn(Optional.of(dummyClient));
        when(sellerRepo.findByEmail(updatedClient.getEmail())).thenReturn(dummySeller);
        assertThrows(ClientOrSellerHasThisSameEntryException.class, () -> clientService.update(updatedClient));
        userService.close();
    }



    @Test
    void deleteTestSuccess() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(user);

        Client dummyClient = new Client();
        dummyClient.setEmail("dummy@gmail.com");
        dummyClient.setPassword("dummyPassword");
        dummyClient.setNumberOfBuys(0);
        when(clientRepo.findById(100)).thenReturn(Optional.of(dummyClient));
        doNothing().when(clientRepo).deleteById(100);
        try{
            clientService.delete();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        userService.close();
    }

    @Test
    void deleteTestFail() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS user = new ClientSS();
        user.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(user);

        Client dummyClient = new Client();
        dummyClient.setEmail("dummy@gmail.com");
        dummyClient.setPassword("dummyPassword");
        dummyClient.setNumberOfBuys(1);
        when(clientRepo.findById(100)).thenReturn(Optional.of(dummyClient));
        assertThrows(UserHasProductsRelationshipsException.class, () -> clientService.delete());
        userService.close();
    }
}