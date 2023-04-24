package com.rene.ecommerce.services;

import com.rene.ecommerce.domain.Order;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.exceptions.AuthorizationException;
import com.rene.ecommerce.exceptions.ObjectNotFoundException;
import com.rene.ecommerce.repositories.OrderRepository;
import com.rene.ecommerce.repositories.SellerRepository;
import com.rene.ecommerce.security.ClientSS;
import com.rene.ecommerce.security.SellerSS;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private ClientService clientService;

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void findByIdTestClientSuccess() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS clientUser = new ClientSS();
        clientUser.setId(100);

        userService.when(UserService::clientAuthenticated).thenReturn(clientUser);
        Client dummyClient = new Client();
        dummyClient.setEmail("zpu2");
        dummyClient.setPassword("123456");
        dummyClient.setId(100);

        Order dummyOrder = new Order();
        dummyOrder.setId(1);
        List<Order> orderList = new ArrayList<>();
        orderList.add(dummyOrder);
        dummyClient.setOrders(orderList);
        when(orderRepo.findById(1)).thenReturn(Optional.of(dummyOrder));
        when(clientService.findById(100)).thenReturn(dummyClient);
        Order returnOrder = orderService.findById(1,true);
        assertEquals(dummyOrder.getId(), returnOrder.getId());
        userService.close();
    }

    // cannot reaches throw objectNotFoundException
    @Test
    void findByIdTestClientObjNotFound() {

    }

    @Test
    void findByIdTestClientDoesNotHaveOrder() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS clientUser = new ClientSS();
        clientUser.setId(100);

        userService.when(UserService::clientAuthenticated).thenReturn(clientUser);
        Client dummyClient = new Client();
        dummyClient.setEmail("zpu2");
        dummyClient.setPassword("123456");
        dummyClient.setId(100);

        Order dummyOrder = new Order();
        dummyOrder.setId(1);
        List<Order> orderList = new ArrayList<>();
        dummyClient.setOrders(orderList);
        when(orderRepo.findById(1)).thenReturn(Optional.of(dummyOrder));
        when(clientService.findById(100)).thenReturn(dummyClient);
        assertThrows(AuthorizationException.class, () -> orderService.findById(1,true));
        userService.close();
    }

    @Test
    void findByIdTestSellerSuccess() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        Seller seller = new Seller();
        seller.setId(1);

        // Mock SellerSS to simulate authenticated seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);
        userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
        Order dummyOrder = new Order();
        dummyOrder.setId(1);
        List<Order> orderList = new ArrayList<>();
        orderList.add(dummyOrder);
        seller.setOrders(orderList);
        when(orderRepo.findById(1)).thenReturn(Optional.of(dummyOrder));
        when(sellerService.findById(1)).thenReturn(seller);
        Order returnOrder = orderService.findById(1,false);
        assertEquals(dummyOrder.getId(), returnOrder.getId());
        userService.close();
    }

    @Test
    void findByIdTestSellerDoesNotHaveOrder() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        Seller seller = new Seller();
        seller.setId(1);

        // Mock SellerSS to simulate authenticated seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);
        userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
        Order dummyOrder = new Order();
        dummyOrder.setId(1);
        List<Order> orderList = new ArrayList<>();
        seller.setOrders(orderList);
        when(orderRepo.findById(1)).thenReturn(Optional.of(dummyOrder));
        when(sellerService.findById(1)).thenReturn(seller);
        assertThrows(AuthorizationException.class, () -> orderService.findById(1,false));
        userService.close();
    }

    @Test
    void findAllClientSuccess() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        ClientSS clientUser = new ClientSS();
        clientUser.setId(100);
        userService.when(UserService::clientAuthenticated).thenReturn(clientUser);
        Client dummyClient = new Client();
        dummyClient.setId(100);
        Order dummyOrder = new Order();
        dummyOrder.setId(1);
        List<Order> orderList = new ArrayList<>();
        orderList.add(dummyOrder);
        dummyClient.setOrders(orderList);
        when(clientService.findById(100)).thenReturn(dummyClient);
        List<Order> orders = orderService.findAll(true);
        assertEquals(orderList.size(),orders.size());
        userService.close();
    }


    @Test
    void findAllSellerSuccess() {
        MockedStatic<UserService> userService = mockStatic(UserService.class);
        Seller seller = new Seller();
        seller.setId(1);

        // Mock SellerSS to simulate authenticated seller
        SellerSS sellerSS = new SellerSS();
        sellerSS.setId(1);
        userService.when(UserService::sellerAuthenticated).thenReturn(sellerSS);
        Order dummyOrder = new Order();
        dummyOrder.setId(1);
        List<Order> orderList = new ArrayList<>();
        orderList.add(dummyOrder);
        seller.setOrders(orderList);
        when(sellerService.findById(1)).thenReturn(seller);
        List<Order> orders = orderService.findAll(false);
        assertEquals(orderList.size(),orders.size());
        userService.close();
    }
}