package com.example.algamoney.api.config;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Profile("oauth-security")
@Configuration
@SuppressWarnings("deprecation")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)// Aqui eu habilito a segurança dos métodos
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	//  A interface abaixoLocaliza o usuário com base no nome de usuário
	private UserDetailsService userDetailsService;
	// Reconfiguraremos aqui o bean do nosso passwordEncoder (que será usado para fazer o decode da senha
	//do usuário e da secret do cliente), utilizando o BCrypt
	
	@Bean
	public PasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	
	}
	
	@Override // Aqui abaixo foi alterado para publico pq o resourceServerConfig é publico também
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		//sobre assuntos
		.antMatchers("/categorias").permitAll()
		.anyRequest().authenticated()
		.and()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
		// Aqui é para fazer javascript injection
		//  Nesta aplicação ele é desabilitado para não ser necessário configurar o token CSRF em todas as requisições
		.csrf().disable()
		//.oauth2ResourceServer().opaqueToken();
		// abaixo não se usa mais opaqueToquen
		.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter());
	}
	
	// Acima e abaixo temos configurações para stateless para garantir.
	
	@Bean
	public JwtDecoder jwtDecoder() {
		// Aqui abaixo tem que ter 32 caracteres para funcionar senão da 401
	    String secretKeyString = "3032885ba9cd6621bcc4e7d6b6c35c2b";
	    // Constrói uma chave secreta de um array de bytes dado.
	    var secretKey = new SecretKeySpec(secretKeyString.getBytes(), "HmacSHA256");
        // O NimbusJwtDecoder será o responsável por, utilizando a mesma secret key configurada no 
	    // Authorization Server, conferir se o token é valido ou não, verificando se a chave que assinou o 
	    // token é a mesma que ele tem configurado
	    return NimbusJwtDecoder.withSecretKey(secretKey).build();
	}
	
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {        
	    return super.authenticationManager();
	}
	
	
	// O JwtAuthenticationConverter será o responsável por realizar a conversão das authorities que chegaram
	//no jwt de uma lista simples de String para GrantedAuthorities. Não tem relação com saber se o token é 
	//válido ou não através da chave de assinatura.
	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
			List<String> authorities = jwt.getClaimAsStringList("authorities");

			if (authorities == null) {
				authorities = Collections.emptyList();
			}
             // A classe abaixo está convertendo as autorizações permitidas para jwt(um tipo de access token)
			//que estão dentro de um token.
			JwtGrantedAuthoritiesConverter scopesAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
			Collection<GrantedAuthority> grantedAuthorities = scopesAuthoritiesConverter.convert(jwt);

			grantedAuthorities.addAll(authorities.stream()
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList()));

			return grantedAuthorities;
		});

		return jwtAuthenticationConverter;
	}
     
	//@Bean
	//@Override
	//public UserDetailsService userDetailsServiceBean() throws Exception {
	//  return super.userDetailsServiceBean();
	//}    
	
	
	
}
