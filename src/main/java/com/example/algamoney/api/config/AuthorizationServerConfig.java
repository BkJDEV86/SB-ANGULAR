package com.example.algamoney.api.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.example.algamoney.api.config.token.CustomTokenEnhancer;

@Profile("oauth-security")
@SuppressWarnings("deprecation")
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig  extends AuthorizationServerConfigurerAdapter {
	
	// é pra chamar um serviço de busca customizado.
	@Autowired
	private UserDetailsService userDetailsService;
	
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
		  .withClient("angular")
		  .secret("$2a$10$2UA.Ey6qNFlnIXUQobjZv.sIrMm5kTqKnApQCuK6NTEuDmvnmtdJC") // @ngul@r0
		  .scopes("read", "write")
		  .authorizedGrantTypes("password", "refresh_token")
		  .accessTokenValiditySeconds(1801)
		  .refreshTokenValiditySeconds(3600*24)
		  .and()
			.withClient("mobile")
			.secret("$2a$10$YjmZSwNETv/rrvUfJPag1eOpzYP3U/xnQ6s.nk3yYe3JCVlGqeYMW") // m0b1l30
			.scopes("read")
			.authorizedGrantTypes("password", "refresh_token")
			.accessTokenValiditySeconds(1801)
		    .refreshTokenValiditySeconds(3600*24);
}
	
	
	@Override
	
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
		
		
		endpoints
		    .tokenStore(tokenStore())
		    // Conversor de Token
		    .accessTokenConverter(accessTokenConverter())
	     	.authenticationManager(authenticationManager)
		     .userDetailsService(userDetailsService)
		     .tokenEnhancer(tokenEnhancerChain)
		        .reuseRefreshTokens(false);
		
	}
	



	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();

		accessTokenConverter.setSigningKey("3032885ba9cd6621bcc4e7d6b6c35c2b");

		return accessTokenConverter;
	}


 //@Override
//     public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//    security.checkTokenAccess("permitAll()");
//}
	
	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
		// aqui abaixo não retorna mais em memória e sim em jwtTokenStore
		//return new InMemoryTokenStore();
		
	}
	
	private TokenEnhancer tokenEnhancer() {
		
		// Customização do token para mostrar o nome do usuário logado na tela
		return new CustomTokenEnhancer();
	}
	

}
