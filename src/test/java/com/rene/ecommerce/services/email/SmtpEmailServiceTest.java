package com.rene.ecommerce.services.email;
import static org.mockito.Mockito.*;

import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpEmailServiceTest {

    @Mock
    private MailSender mailSender;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private SmtpEmailService smtpEmailService;

    @Test
    public void testSendEmail() {
        MockitoAnnotations.initMocks(this);
        SimpleMailMessage msg = new SimpleMailMessage();
        smtpEmailService.sendEmail(msg);
        verify(mailSender, times(1)).send(msg);
    }

    @Test
    public void testSendEmailHtml() {
        MockitoAnnotations.initMocks(this);
        MimeMessage msg = mock(MimeMessage.class);
        smtpEmailService.sendEmailHtml(msg);
        verify(javaMailSender, times(1)).send(msg);
    }

}

