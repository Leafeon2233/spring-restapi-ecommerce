package com.rene.ecommerce.services.email;

import com.rene.ecommerce.domain.Product;
import com.rene.ecommerce.domain.users.Client;
import com.rene.ecommerce.domain.users.Seller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.TestPropertySource;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@SpringBootTest
@TestPropertySource(properties = "default.sender=test@example.com")
@ExtendWith(MockitoExtension.class)
public class AbstractEmailServiceTest {

    @Mock
    private MailSender mailSender;

    @Mock
    private EmailService emailServiceMock;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessageHelper mimeMessageHelper;


    @InjectMocks
    private AbstractEmailService emailService = new SmtpEmailService();

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailMessageCaptor;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendNewPassword() {
        String email = "test@example.com";
        String newPassword = "test_password";
        emailService.sendNewPassword(email, newPassword);
        verify(mailSender).send(simpleMailMessageCaptor.capture());
        SimpleMailMessage simpleMailMessage = simpleMailMessageCaptor.getValue();
        assert(simpleMailMessage.getTo()[0].equals(email));
        assert(simpleMailMessage.getSubject().equals("New password"));
        assert(simpleMailMessage.getText().equals("New password: " + newPassword));
    }

    @Test
    public void sendConfirmationEmail_shouldSendEmailsToBuyerAndSeller() {
        // create a sample Product for testing
        Product product = new Product();
        product.setPrice(100.0);
        product.setId(0);
        product.setName("Test Product");
        product.setDescription("This is a test product");

        Client client = new Client();
        client.setId(0);
        client.setEmail("client@gmail.com");
        client.setName("Client");

        Seller seller = new Seller();
        seller.setId(1);
        seller.setEmail("seller@gmail.com");
        seller.setName("Seller");
        product.setBuyerOfTheProduct(client);
        product.setProductOwner(seller);

        // call the method being tested
        emailService.sendConfirmationEmail(product);

        // Since we cannot mock the System.currentTimeMillis() method, we choose to verify arguments one by one
        // Extract the send() call arguments
        verify(mailSender, times(2)).send(simpleMailMessageCaptor.capture());
        assertEquals(simpleMailMessageCaptor.getAllValues().get(0).getTo()[0],
                product.getBuyerOfTheProduct().getEmail());
        assertEquals(simpleMailMessageCaptor.getAllValues().get(0).getSubject(),
                "You order has been completed");
        assertEquals(simpleMailMessageCaptor.getAllValues().get(0).getText(),
                product.toString());
        assertEquals(simpleMailMessageCaptor.getAllValues().get(1).getTo()[0],
                product.getProductOwner().getEmail());
        assertEquals(simpleMailMessageCaptor.getAllValues().get(1).getSubject(),
                "Someone has bought your product");
        assertEquals(simpleMailMessageCaptor.getAllValues().get(1).getText(),
                product.getBuyerOfTheProduct().getName() + " bought your product");
    }

    @Test
    public void sendConfirmationEmailHtml_shouldSendEmailsToBuyerAndSeller() throws MessagingException {
        Product product = new Product();
        product.setPrice(100.0);
        product.setId(0);
        product.setName("Test Product");
        product.setDescription("This is a test product");

        Client client = new Client();
        client.setId(0);
        client.setEmail("client@gmail.com");
        client.setName("Client");

        Seller seller = new Seller();
        seller.setId(1);
        seller.setEmail("seller@gmail.com");
        seller.setName("Seller");
        product.setBuyerOfTheProduct(client);
        product.setProductOwner(seller);

        doReturn(new MimeMessage((Session) null)).when(javaMailSender).createMimeMessage();
        doNothing().when(javaMailSender).send(any(MimeMessage.class));

        emailService.sendConfirmationEmailHtml(product);

        verify(javaMailSender, times(2)).send(mimeMessageCaptor.capture());
    }
}

