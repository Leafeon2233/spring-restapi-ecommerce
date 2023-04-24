package com.rene.ecommerce.services.details;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.repositories.ClientRepository;
import com.rene.ecommerce.repositories.SellerRepository;
import com.rene.ecommerce.security.ClientSS;
import com.rene.ecommerce.security.SellerSS;

public class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private ClientRepository clientRepo;

    @Mock
    private SellerRepository sellerRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void loadUserByUsername_Client() {
        // Create mock client
        Client client = new Client();
        client.setId(1);
        client.setEmail("client@example.com");
        client.setPassword("client_password");
        client.setType("CLIENT");

        // Define behavior for clientRepo mock
        when(clientRepo.findByEmail("client@example.com")).thenReturn(client);

        // Test method
        UserDetails userDetails = userDetailsService.loadUserByUsername("client@example.com");

        // Assertions
        assertEquals(ClientSS.class, userDetails.getClass());
        assertEquals(client.getEmail(), userDetails.getUsername());
        assertEquals(client.getPassword(), userDetails.getPassword());
    }

    @Test
    public void loadUserByUsername_Seller() {
        // Create mock seller
        Seller seller = new Seller();
        seller.setId(1);
        seller.setEmail("seller@example.com");
        seller.setPassword("seller_password");
        seller.setType("SELLER");

        // Define behavior for clientRepo and sellerRepo mock
        when(clientRepo.findByEmail("seller@example.com")).thenReturn(null);
        when(sellerRepo.findByEmail("seller@example.com")).thenReturn(seller);

        // Test method
        UserDetails userDetails = userDetailsService.loadUserByUsername("seller@example.com");

        // Assertions
        assertEquals(SellerSS.class, userDetails.getClass());
        assertEquals(seller.getEmail(), userDetails.getUsername());
        assertEquals(seller.getPassword(), userDetails.getPassword());
    }
}
