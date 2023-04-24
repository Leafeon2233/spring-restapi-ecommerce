package com.rene.ecommerce.services;

import com.rene.ecommerce.domain.Product;
import com.rene.ecommerce.domain.users.Client;

import com.rene.ecommerce.exceptions.ProductHasAlreadyBeenSold;
import com.rene.ecommerce.exceptions.YouHaveAlreadyAddThisProductInYourWishlistException;
import com.rene.ecommerce.repositories.ClientRepository;
import com.rene.ecommerce.repositories.ProductRepository;
import com.rene.ecommerce.security.ClientSS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;


import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;


class WishlistServiceTest {

    @InjectMocks
    private WishlistService wishlistService;

    @Mock
    private ClientService clientService;

    @Mock
    private ProductService productService;
    

    @Mock
    private ClientRepository clientRepo;

    @Mock
    private ProductRepository productRepo;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }
    @AfterEach
    void tearDown() {
    }

    @Test
    public void testFetchAll() {
        // Create a mock client and a set of products
        Client client = new Client();
        client.setId(1);

        Product product1 = new Product();
        product1.setId(1);

        Product product2 = new Product();
        product2.setId(2);

        Set<Product> productsWished = new HashSet<>();
        productsWished.add(product1);
        productsWished.add(product2);

        client.setProductsWished(productsWished);

        // Mock ClientSS to simulate authenticated client
        ClientSS clientSS = new ClientSS();

        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);
            when(clientService.findById(clientSS.getId())).thenReturn(client);
            Set<Product> result = wishlistService.findAll();
            assertEquals(productsWished, result);

        } catch (Exception e){
            System.out.println(e);
        }
    }

    @Test
    public void testMarkProductAsWished() {
        // Create a mock client and a mock product
        Client client = new Client();
        client.setId(1);
        client.setProductsWished(new HashSet<>());

        Product product = new Product();
        product.setId(101);
        product.setWhoWhishesThisProduct(new HashSet<>());

        Product product2 = new Product();
        product2.setId(102);
        product2.setHasBeenSold("Sold");
        product2.setBuyerOfTheProduct(client);

        // Mock ClientSS to simulate authenticated client
        ClientSS clientSS = new ClientSS();

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);
            when(clientService.findById(clientSS.getId())).thenReturn(client);
            when(productService.findById(product.getId())).thenReturn(product);

            // Call the method under test
            wishlistService.markProductAsWished(product.getId());

            // Assert that the product was added to the client's wishlist
            // and the repositories were called to save the changes
            verify(clientRepo).save(client);
            verify(productRepo).save(product);

            // assert that repeating adding an added product leads to an exception
            assertThrows(YouHaveAlreadyAddThisProductInYourWishlistException.class,
                    () -> wishlistService.markProductAsWished(product.getId()));
        } catch (Exception e){
            System.out.println(e);
        }
    }

    @Test
    public void testMarkASoldProductAsWished() {
        // Create a mock client and a mock product
        Client client = new Client();
        client.setId(1);
        client.setProductsWished(new HashSet<>());

        Product product2 = new Product();
        product2.setId(102);
        product2.setHasBeenSold("Sold");
        product2.setBuyerOfTheProduct(client);

        // Mock ClientSS to simulate authenticated client
        ClientSS clientSS = new ClientSS();

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);
            when(clientService.findById(clientSS.getId())).thenReturn(client);
            when(productService.findById(product2.getId())).thenReturn(product2);

            assertThrows(ProductHasAlreadyBeenSold.class,
                    () -> wishlistService.markProductAsWished(product2.getId()));
        } catch (Exception e){
            System.out.println(e);
        }
    }

    @Test
    public void testDelete() {
        // Mock ClientSS to simulate authenticated client
        ClientSS clientSS = new ClientSS();
        clientSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);

            // Define productId to be removed from the wishlist
            Integer productId = 101;

            // Call the method under test
            wishlistService.delete(productId);

            // Assert that the product was removed from the client's wishlist by calling the appropriate repository method
            verify(productRepo).removeFromClientWishlist(productId, 1);
        }
    }

    @Test
    void removeProductFromWishlistWhenIsSold() {
        // Mock ClientSS to simulate authenticated client
        ClientSS clientSS = new ClientSS();
        clientSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);

            // Define productId to be removed from the wishlist
            Integer productId = 101;

            // Call the method under test
            wishlistService.removeProductFromWishlistWhenIsSold(productId);

            // Assert that the product was removed from the client's wishlist by calling the appropriate repository method
            verify(productRepo).removeFromWishListWhenIsSold(productId);
        }

    }
}