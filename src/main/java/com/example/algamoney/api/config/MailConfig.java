package com.example.algamoney.api.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.example.algamoney.api.config.property.AlgamoneyApiProperty;

@Configuration
public class MailConfig {

	@Autowired
	private AlgamoneyApiProperty property;

	// A STARTTLS é uma extensão de vários protocolos de comunicação, incluindo IMAP, POP3 SMTP, FTP e XMPP, e permite que uma
	//conexão de texto puro seja atualizada para uma criptografada, usando os protocolos TLS (Transport Layer Security) ou SSL 
	//(Secure Sockets Layer).
	@Bean
	public JavaMailSender javaMailSender() {
		// The Properties can be saved to a streamor loaded from a stream. Each key and its corresponding value inthe property 
		//list is a string. 
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", true);
		props.put("mail.smtp.starttls.enable", true);
		props.put("mail.smtp.connectiontimeout", 10000);// Tempo de espera de uma coneção para envio de email!

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setJavaMailProperties(props);
		mailSender.setHost(property.getMail().getHost());
		mailSender.setPort(property.getMail().getPort());
		mailSender.setUsername(property.getMail().getUsername());
		mailSender.setPassword(property.getMail().getPassword());

		return mailSender;
	}
}