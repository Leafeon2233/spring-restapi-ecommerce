package com.rene.ecommerce.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.rene.ecommerce.domain.dto.ranking.SellerRankingDTO;
import com.rene.ecommerce.domain.users.Seller;
import com.rene.ecommerce.repositories.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rene.ecommerce.domain.dto.ranking.ClientRankingDTO;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.repositories.ClientRepository;

public class RankingServiceTest {

    @InjectMocks
    private RankingService rankingService;

    @Mock
    private ClientRepository clientRepo;

    @Mock
    private SellerRepository sellerRepo;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testReturnRankingClient() {
        // Create mock clients
        Client client1 = new Client();
        client1.setId(1);
        client1.setName("Client 1");
        client1.setNumberOfBuys(10);
        client1.setHowMuchMoneyThisClientHasSpent(100.0);

        Client client2 = new Client();
        client2.setId(2);
        client2.setName("Client 2");
        client2.setNumberOfBuys(5);
        client2.setHowMuchMoneyThisClientHasSpent(50.0);

        // Define behavior of mocked methods
        when(clientRepo.returnRankingClient()).thenReturn(Arrays.asList(client1, client2));

        // Call the method under test
        List<ClientRankingDTO> rankingDTO = rankingService.returnRankingClient();

        // Assert that the correct rankings are returned
        assertEquals(2, rankingDTO.size());
        assertEquals(1, rankingDTO.get(0).getId());
        assertEquals("Client 1", rankingDTO.get(0).getName());
        assertEquals(2, rankingDTO.get(1).getId());
        assertEquals("Client 2", rankingDTO.get(1).getName());
    }

    @Test
    public void testReturnRankingSeller() {
        // Create mock clients
        Seller seller1 = new Seller();
        seller1.setId(1);
        seller1.setName("Seller 1");
        seller1.setNumberOfSells(10);
        seller1.setHowMuchMoneyThisSellerHasSold(100.0);

        Seller sellerr2 = new Seller();
        sellerr2.setId(2);
        sellerr2.setName("Seller 2");
        sellerr2.setNumberOfSells(5);
        sellerr2.setHowMuchMoneyThisSellerHasSold(50.0);

        // Define behavior of mocked methods
        when(sellerRepo.returnRankingSeller()).thenReturn(Arrays.asList(seller1, sellerr2));

        // Call the method under test
        List<SellerRankingDTO> rankingDTO = rankingService.returnRankingSeller();

        // Assert that the correct rankings are returned
        assertEquals(2, rankingDTO.size());
        assertEquals(1, rankingDTO.get(0).getId());
        assertEquals("Seller 1", rankingDTO.get(0).getName());
        assertEquals(2, rankingDTO.get(1).getId());
        assertEquals("Seller 2", rankingDTO.get(1).getName());
    }
}
