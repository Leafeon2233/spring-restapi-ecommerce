package com.rene.ecommerce.services;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.rene.ecommerce.exceptions.ClientOrSellerHasThisSameEntryException;
import com.rene.ecommerce.exceptions.DuplicateEntryException;
import com.rene.ecommerce.repositories.ClientRepository;
import com.rene.ecommerce.repositories.SellerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.exceptions.AuthorizationException;
import com.rene.ecommerce.exceptions.ObjectNotFoundException;
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
    public void testFindById_AuthorizationException() {
        // Mock SellerSS to simulate unauthorized seller
        SellerSS sellerSS = new SellerSS();

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(null); // should return null?
            when(sellerRepo.findById(2)).thenReturn(Optional.of(new Seller()));

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
    }

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



}
