package com.rene.ecommerce.services;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.rene.ecommerce.exceptions.*;
import com.rene.ecommerce.repositories.ClientRepository;
import com.rene.ecommerce.repositories.SellerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.rene.ecommerce.domain.dto.updated.UpdatedSeller;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.security.SellerSS;

public class SellerServiceTest {

    @InjectMocks
    private SellerService sellerService;

    @Mock
    private SellerRepository sellerRepo;

    @Mock
    private ClientRepository clientRepo;

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindById_Success() {
        // Create a mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Mock SellerSS to simulate authenticated seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(Optional.of(seller));

            // Call the method under test
            Seller foundSeller = sellerService.findById(sellerSS.getId());

            // Assert that the correct seller is returned
            assertEquals(seller, foundSeller);
        }
    }

    @Test
    public void testFindById_AuthorizationExceptionWithoutLogginging() {
        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            // Mock SellerSS to simulate without logging in
            userService.when(UserService::sellerAuthenticated).thenReturn(null);
            when(sellerRepo.findById(2)).thenReturn(Optional.of(new Seller()));

            // Assert that the AuthorizationException is thrown
            assertThrows(AuthorizationException.class, () -> {
                sellerService.findById(2);
            });
        }
    }

    @Test
    public void testFindById_AuthorizationExceptionQueryingDifferentAccount() {
        // Mock SellerSS to
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            // simulate logging in account 1 but querying account 2
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);

            // Assert that the AuthorizationException is thrown
            assertThrows(AuthorizationException.class, () -> {
                sellerService.findById(2);
            });
        }
    }

    @Test
    public void testFindById_ObjectNotFoundException() {
        // Mock SellerSS to simulate authorized seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(1)).thenReturn(Optional.empty());

            // Assert that the ObjectNotFoundException is thrown
            assertThrows(ObjectNotFoundException.class, () -> {
                sellerService.findById(1);
            });
        }
    }

    // Return seller without parsing the id
    // api: /seller
    @Test
    public void testReturnClientWithoutParsingTheId_Success() {
        // Create a mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Mock SellerSS to simulate authenticated seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(Optional.of(seller));

            // Call the method under test
            Seller foundSeller = sellerService.returnClientWithoutParsingTheId();

            // Assert that the correct seller is returned
            assertEquals(seller, foundSeller);
        }
    }

    @Test
    public void testReturnClientWithoutParsingTheId_AuthorizationException() {
        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(null);

            // Assert that the AuthorizationException is thrown
            assertThrows(AuthorizationException.class, () -> {
                sellerService.returnClientWithoutParsingTheId();
            });
        }
    }

    @Test
    public void testReturnClientWithoutParsingTheId_ObjectNotFoundException() {
        // Mock SellerSS to simulate authorized seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(1)).thenReturn(Optional.empty());

            // Assert that the ObjectNotFoundException is thrown
            assertThrows(ObjectNotFoundException.class, () -> {
                sellerService.returnClientWithoutParsingTheId();
            });
        }
        // Impossible to reach
    }

    // Return all sellers
    // api: /sellers
    @Test
    public void testFindAll() {
        // Create mock sellers
        Seller seller1 = new Seller();
        seller1.setId(1);
        Seller seller2 = new Seller();
        seller2.setId(2);

        // Define a list of sellers to be returned by the repository
        List<Seller> sellers = Arrays.asList(seller1, seller2);

        // Define behavior of mocked methods
        when(sellerRepo.findAll()).thenReturn(sellers);

        // Call the method under test
        List<Seller> foundSellers = sellerService.findAll();

        // Assert that the correct list of sellers is returned
        assertEquals(sellers, foundSellers);
    }

    // Create a seller
    // api: /create/seller
    @Test
    public void testInsert_Success() {
        // Create a mock seller
        Seller seller = new Seller();
        seller.setId(null);
        seller.setEmail("test@example.com");
        String rawPassword = "password";
        seller.setPassword(rawPassword);

        // Define behavior of mocked methods
        when(clientRepo.findByEmail(seller.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn("encoded_password"); // Define behavior of the encode method
        when(sellerRepo.save(seller)).thenReturn(seller);

        // Call the method under test
        Seller savedSeller = sellerService.insert(seller);

        // Assert that the correct seller is saved
        assertEquals(seller, savedSeller);
        assertEquals("encoded_password", savedSeller.getPassword()); // Verify that the password is encoded
    }

    @Test
    public void testInsert_DuplicateEntryException() {
        // Create a mock seller
        Seller seller = new Seller();
        seller.setEmail("test@example.com");
        seller.setPassword("password");

        // Define behavior of mocked methods
        when(clientRepo.findByEmail(seller.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(seller.getPassword())).thenReturn("encoded_password");
        when(sellerRepo.save(seller)).thenThrow(RuntimeException.class);

        // Assert that the DuplicateEntryException is thrown
        assertThrows(DuplicateEntryException.class, () -> {
            sellerService.insert(seller);
        });
    }

    @Test
    public void testInsert_ClientOrSellerHasThisSameEntryException() {
        // Create a mock seller
        Seller seller = new Seller();
        seller.setEmail("test@example.com");
        seller.setPassword("password");
        // Create a mock client that has already exists
        Client client = new Client();

        // Define behavior of mocked methods
        when(clientRepo.findByEmail(seller.getEmail())).thenReturn(client);
        when(passwordEncoder.encode(seller.getPassword())).thenReturn("encoded_password");

        // Assert that the ClientOrSellerHasThisSameEntryException is thrown
        assertThrows(ClientOrSellerHasThisSameEntryException.class, () -> {
            sellerService.insert(seller);
        });
    }

    // Update a seller
    // api: /update/seller
    @Test
    public void testUpdate_ClientOrSellerHasThisSameEntryException() {
        // Create a mock updated seller
        UpdatedSeller updatedSeller = new UpdatedSeller();
        updatedSeller.setEmail("updated@example.com");
        updatedSeller.setName("Updated Seller");
        updatedSeller.setPassword("updated_password");

        // Create a mock seller SS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Create a mock seller
        Seller seller = new Seller();
        seller.setEmail("test@example.com");
        seller.setPassword("password");
        seller.setId(1);

        // Define behavior of mocked methods
        //Test the situation that an email already exists when updating this email to a seller
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(Optional.of(seller));
            when(passwordEncoder.encode(updatedSeller.getPassword())).thenReturn("encoded_updated_password");
            when(clientRepo.findByEmail(updatedSeller.getEmail())).thenReturn(new Client());
            when(sellerRepo.save(seller)).thenReturn(seller);

            // Assert that the ClientOrSellerHasThisSameEntryException is thrown
            assertThrows(ClientOrSellerHasThisSameEntryException.class, () -> {
                sellerService.update(updatedSeller);
            });
        }
    }

    @Test
    public void testUpdate_DuplicateEntryException() {
        // Create a mock updated seller
        UpdatedSeller updatedSeller = new UpdatedSeller();
        updatedSeller.setEmail("updated@example.com");
        updatedSeller.setName("Updated Seller");
        updatedSeller.setPassword("updated_password");

        // Create a mock seller SS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Create a mock seller
        Seller seller = new Seller();
        seller.setEmail("test@example.com");
        seller.setPassword("password");
        seller.setId(1);

        // Test any other situations at the database side that causes a duplicate entry exception
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(Optional.of(seller));
            when(clientRepo.findByEmail(updatedSeller.getEmail())).thenReturn(null);
            when(passwordEncoder.encode(updatedSeller.getPassword())).thenReturn("encoded_updated_password");
            when(sellerRepo.save(seller)).thenThrow(RuntimeException.class);

            // Assert that the DuplicateEntryException is thrown
            assertThrows(DuplicateEntryException.class, () -> {
                sellerService.update(updatedSeller);
            });
        }
    }

    @Test
    public void testUpdate_AuthorizationException_RequestUpdatingDifferentSellerProfile() {
        // Create a mock seller contains updated information
        UpdatedSeller updatedSeller = new UpdatedSeller();
        updatedSeller.setEmail("updated@example.com");
        updatedSeller.setPassword("updated_password");

        // Create a mock seller SS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Create a mock seller
        Seller seller = new Seller();
        seller.setEmail("test@example.com");
        seller.setPassword("password");
        seller.setId(2);


        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(Optional.of(seller));

            // Assert that the AuthorizationException is thrown
            assertThrows(AuthorizationException.class, () -> {
                sellerService.update(updatedSeller);
            });
        }
    }

    // Failed test case
    @Test
    public void testUpdate_AuthorizationException_RequestUpdatingWithoutLoggingIn() {
        // Create a mock seller contains updated information
        UpdatedSeller updatedSeller = new UpdatedSeller();
        updatedSeller.setEmail("updated@example.com");
        updatedSeller.setPassword("updated_password");

        // Create a mock seller that need to be modified
        int sellerId = 2;
        Seller seller = new Seller();
        seller.setEmail("test@example.com");
        seller.setPassword("password");
        seller.setId(sellerId);


        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            // without logging in
            userService.when(UserService::sellerAuthenticated).thenReturn(null);
            when(sellerRepo.findById(sellerId)).thenReturn(Optional.of(seller));

            // Assert that the AuthorizationException is thrown
            assertThrows(AuthorizationException.class, () -> {
                sellerService.update(updatedSeller);
            });
        }
    }

    @Test
    public void testDelete_Success() {
        // Create a mock seller SS
        int sellerId = 2;
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(sellerId);

        // Create a mock seller with zero number of sells
        Seller seller = new Seller();
        seller.setNumberOfSells(0);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(java.util.Optional.of(seller));

            // Call the method under test
            sellerService.delete();

            // No exception should be thrown
        }
    }

    @Test
    public void testDelete_UserHasProductsRelationshipsException() {
        // Create a mock seller SS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(2);

        // Create a mock seller with non-zero number of sells
        Seller seller = new Seller();
        seller.setNumberOfSells(5);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(java.util.Optional.of(seller));

            // Assert that the UserHasProductsRelationshipsException is thrown
            assertThrows(UserHasProductsRelationshipsException.class, () -> {
                sellerService.delete();
            });
        }
    }


    // Failed test case
    // Should throw AuthorizationException, but throw nullpointexception instead
    @Test
    public void testDeleteWithoutLoggingin() {
        // Create a mock seller that need to be modified
        int sellerId = 1;
        Seller seller = new Seller();
        seller.setEmail("test@example.com");
        seller.setPassword("password");
        seller.setId(sellerId);

        // Create a mock seller SS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(sellerId);

        // Test trying to delete a seller without logging in
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(null);
            when(sellerRepo.findById(sellerSS.getId())).thenReturn(Optional.of(seller));

            // Assert that the DuplicateEntryException is thrown
            assertThrows(DuplicateEntryException.class, () -> {
                sellerService.delete();
            });
        }
    }
}
