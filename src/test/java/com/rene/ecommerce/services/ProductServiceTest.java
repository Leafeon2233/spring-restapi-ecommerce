package com.rene.ecommerce.services;

import com.rene.ecommerce.domain.Product;
import com.rene.ecommerce.domain.dto.updated.UpdatedProduct;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.repositories.ProductRepository;
import com.rene.ecommerce.exceptions.AuthorizationException;
import com.rene.ecommerce.exceptions.ObjectNotFoundException;
import com.rene.ecommerce.exceptions.ProductHasAlreadyBeenSold;

import com.rene.ecommerce.security.ClientSS;
import com.rene.ecommerce.security.SellerSS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private SellerService sellerService;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Return a product by id
    // API: /product/{id}
    @Test
    public void testFindById_Success() {
        // Create mock product
        Product product = new Product();
        product.setId(1);
        product.setName("Product 1");

        // Define behavior of mocked methods
        when(productRepo.findById(1)).thenReturn(Optional.of(product));

        // Call the method under test
        Product foundProduct = productService.findById(1);

        // Assert that the correct product is returned
        assertEquals(1, foundProduct.getId());
        assertEquals("Product 1", foundProduct.getName());
    }

    @Test
    public void testFindById_ObjectNotFoundException() {
        // Define behavior of mocked methods
        when(productRepo.findById(1)).thenReturn(Optional.empty());

        // Assert that the ObjectNotFoundException is thrown
        assertThrows(ObjectNotFoundException.class, () -> {
            productService.findById(1);
        });
    }

    @Test
    public void testInsert_Success() {
        // Create mock product
        Product product = new Product();
        product.setName("Product 1");
        product.setHasBeenSold("Unsold");

        // Create mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Create mock SellerSS
        SellerSS sellerSS = new SellerSS();

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerService.findById(sellerSS.getId())).thenReturn(seller);
            when(productRepo.save(product)).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(1);
                return p;
            });

            // Call the method under test
            Product insertedProduct = productService.insert(product);

            // Assert that the correct product is inserted
            assertEquals(1, insertedProduct.getId());
            assertEquals("Product 1", insertedProduct.getName());
            assertEquals("Unsold", insertedProduct.hasBeenSold());
        }
    }

    // Failed due to NullPointerException
    @Test
    public void testInsert_WithoutAutentication() {
        // Create mock product
        Product product = new Product();
        product.setName("Product 1");
        product.setHasBeenSold("Unsold");

        // Create mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Create mock SellerSS
        SellerSS sellerSS = new SellerSS();

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(null);
            when(productRepo.save(product)).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(1);
                return p;
            });

            // Call the method under test
            Product insertedProduct = productService.insert(product);

            // Assert that the AuthorizationException is thrown
            assertThrows(AuthorizationException.class, () -> {
                sellerService.findById(2);
            });
        }
    }


    @Test
    public void testUpdate_Success() {
        // Create mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Create mock SellerSS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Create existing product
        Product existingProduct = new Product();
        existingProduct.setId(1);
        existingProduct.setName("Old Product");
        existingProduct.setHasBeenSold("Unsold");
        existingProduct.setProductOwner(seller);

        // Create updated product data
        UpdatedProduct updatedProductData = new UpdatedProduct();
        updatedProductData.setName("New Product");
        updatedProductData.setDescription("New Description");
        updatedProductData.setPrice(100.0);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerService.findById(sellerSS.getId())).thenReturn(seller);
            when(productRepo.findById(existingProduct.getId())).thenReturn(Optional.of(existingProduct));
            when(productRepo.save(existingProduct)).thenReturn(existingProduct);

            // Call the method under test
            Product updatedProduct = productService.update(updatedProductData, existingProduct.getId());

            // Assert that the product is updated correctly
            assertEquals(1, updatedProduct.getId());
            assertEquals("New Product", updatedProduct.getName());
            assertEquals("New Description", updatedProduct.getDescription());
            assertEquals(100.0, updatedProduct.getPrice());
        }
    }

    @Test
    public void testUpdate_FailedDueToProductAlreadyBeenSold() {
        // Create mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Create mock SellerSS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Create existing product that has been bought
        Product existingProduct = new Product();
        existingProduct.setId(1);
        existingProduct.setName("Old Product");
        existingProduct.setHasBeenSold("Sold");
        existingProduct.setProductOwner(seller);
        existingProduct.setBuyerOfTheProduct(new Client());

        // Create updated product data
        UpdatedProduct updatedProductData = new UpdatedProduct();
        updatedProductData.setName("New Product");
        updatedProductData.setDescription("New Description");
        updatedProductData.setPrice(100.0);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerService.findById(sellerSS.getId())).thenReturn(seller);
            when(productRepo.findById(existingProduct.getId())).thenReturn(Optional.of(existingProduct));
            when(productRepo.save(existingProduct)).thenReturn(existingProduct);

            // Call the method under test
            // Assert that an exception is thrown
            assertThrows(ProductHasAlreadyBeenSold.class, () -> {
                productService.update(updatedProductData, existingProduct.getId());
            });
        }
    }

    @Test
    public void testUpdate_FailedDueToUpdatingProductNotOwned() {
        // Create mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Create mock SellerSS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Create mock seller 2
        Seller seller2 = new Seller();
        seller2.setId(2);

        // Create existing product that has been bought
        Product existingProduct = new Product();
        existingProduct.setId(1);
        existingProduct.setName("Old Product");
        existingProduct.setHasBeenSold("Sold");
        existingProduct.setProductOwner(seller2);

        // Create updated product data
        UpdatedProduct updatedProductData = new UpdatedProduct();
        updatedProductData.setName("New Product");
        updatedProductData.setDescription("New Description");
        updatedProductData.setPrice(100.0);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerService.findById(sellerSS.getId())).thenReturn(seller);
            when(productRepo.findById(existingProduct.getId())).thenReturn(Optional.of(existingProduct));
            when(productRepo.save(existingProduct)).thenReturn(existingProduct);

            // Call the method under test
            // Assert that an exception is thrown
            assertThrows(AuthorizationException.class, () -> {
                productService.update(updatedProductData, existingProduct.getId());
            });
        }
    }
    @Test
    public void testDelete() {
        // Create mock seller
        Seller seller = new Seller();
        seller.setId(1);

        // Create existing product
        Product existingProduct = new Product();
        existingProduct.setId(1);
        existingProduct.setName("Product to Delete");
        existingProduct.setHasBeenSold("Unsold");
        existingProduct.setProductOwner(seller);

        // Create product not owned
        Product product2 = new Product();
        product2.setId(2);
        product2.setName("Product not owned");
        product2.setHasBeenSold("Unsold");
        product2.setProductOwner(new Seller());

        // Create product that has been sold
        Product product3 = new Product();
        product3.setId(3);
        product3.setName("Product that has been sold");
        product3.setHasBeenSold("Sold");
        product3.setProductOwner(seller);
        product3.setBuyerOfTheProduct(new Client());

        // Create mock SellerSS
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        // Define behavior of mocked methods
        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerService.findById(sellerSS.getId())).thenReturn(seller);
            when(productRepo.findById(existingProduct.getId())).thenReturn(Optional.of(existingProduct));
            when(productRepo.findById(product2.getId())).thenReturn(Optional.of(product2));
            when(productRepo.findById(product3.getId())).thenReturn(Optional.of(product3));

            // Call the method under test
            productService.delete(existingProduct.getId());

            // Verify that the deleteById method was called with the correct ID
            verify(productRepo, times(1)).deleteById(existingProduct.getId());

            // When deleting the product not owned, should throw an exception
            assertThrows(AuthorizationException.class, () -> {
                productService.delete(product2.getId());
            });

            // When deleting the product not owned, should throw an exception
            assertThrows(ProductHasAlreadyBeenSold.class, () -> {
                productService.delete(product3.getId());
            });
        }
    }


    @Test
    public void testFindAll_Success() {
        // Create mock products
        Product product1 = new Product();
        product1.setId(1);
        product1.setName("Product 1");
        product1.setHasBeenSold("Unsold");

        Product product2 = new Product();
        product2.setId(2);
        product2.setName("Product 2");
        product2.setHasBeenSold("Unsold");

        // Define behavior of mocked methods
        when(productRepo.findByHasBeenSold("Unsold")).thenReturn(Arrays.asList(product1, product2));

        // Call the method under test
        List<Product> products = productService.findAll();

        // Assert that the correct products are returned
        assertEquals(Arrays.asList(product1, product2), products);
    }


    @Test
    public void testFindOwnProducts_Success() {
        // Create mock seller and products
        Seller seller = new Seller();
        seller.setId(1);
        seller.setName("Seller 1");

        Product product1 = new Product();
        product1.setId(1);
        product1.setName("Product 1");
        product1.setProductOwner(seller);

        Product product2 = new Product();
        product2.setId(2);
        product2.setName("Product 2");
        product2.setProductOwner(seller);

        seller.setOwnProducts(Arrays.asList(product1, product2));

        // Create mock authenticated seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);

        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
            when(sellerService.findById(sellerSS.getId())).thenReturn(seller);

            // Call the method under test
            List<Product> ownProducts = productService.findOwnProducts();

            // Assert that the correct list of own products is returned
            assertEquals(Arrays.asList(product1, product2), ownProducts);
        }
    }
    @Test
    public void testBuyProduct_Success() {
        // Create mock client and seller
        Client client = new Client();
        client.setId(1);
        client.setName("Client 1");
        client.setHowMuchMoneyThisClientHasSpent(0.0);

        Seller seller = new Seller();
        seller.setId(1);
        seller.setName("Seller 1");
        seller.setHowMuchMoneyThisSellerHasSold(0.0);

        // Create mock product
        Product product = new Product();
        product.setId(1);
        product.setName("Product 1");
        product.setProductOwner(seller);
        product.setHasBeenSold("Unsold");
        product.setPrice(100.0);

        // Create mock authenticated client
        ClientSS clientSS = new ClientSS();
        clientSS.setId(1);

        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);
            when(clientService.findById(clientSS.getId())).thenReturn(client);
            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));
            when(productRepo.save(product)).thenReturn(product);

            // Call the method under test
            Product boughtProduct = productService.buyProduct(product.getId());

            // Assert that the product is successfully bought
            assertEquals("Sold", boughtProduct.hasBeenSold());
            assertEquals(client, boughtProduct.getBuyerOfTheProduct());
        }
    }

    @Test
    public void testBuyProduct_ProductHasAlreadyBeenSold() {
        // Create mock product that has been sold
        Product product = new Product();
        product.setId(1);
        product.setName("Product 1");
        product.setHasBeenSold("Sold");
        product.setBuyerOfTheProduct(new Client());
        product.setProductOwner(new Seller());
        product.setPrice(100.0);

        // Create mock authenticated client
        ClientSS clientSS = new ClientSS();
        clientSS.setId(1);

        // Create mock client and seller
        Client client = new Client();
        client.setId(1);
        client.setName("Client 1");
        client.setHowMuchMoneyThisClientHasSpent(0.0);

        try (MockedStatic<UserService> userService = Mockito.mockStatic(UserService.class)) {
            userService.when(UserService::clientAuthenticated).thenReturn(clientSS);
            when(clientService.findById(1)).thenReturn(client);
            when(productRepo.findById(product.getId())).thenReturn(Optional.of(product));
            when(productRepo.save(product)).thenReturn(product);

            // Assert that the ProductHasAlreadyBeenSold exception is thrown
            assertThrows(ProductHasAlreadyBeenSold.class, () -> {
                productService.buyProduct(product.getId());
            });
        }
    }

    @Test
    public void testBuyProduct_DoesNotExists() {
        when(productRepo.findById(10000)).thenReturn(Optional.empty());

        // Assert that the ProductHasAlreadyBeenSold exception is thrown
        assertThrows(ObjectNotFoundException.class, () -> {
            productService.buyProduct(10000);
        });
    }
}