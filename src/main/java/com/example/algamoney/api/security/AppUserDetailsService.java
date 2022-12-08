package com.example.algamoney.api.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.algamoney.api.model.Usuario;
import com.example.algamoney.api.repository.UsuarioRepository;
import com.example.algamoney.api.security.util.UsuarioSistema;

@Service
public class AppUserDetailsService implements UserDetailsService {

	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	
	@Override   // o método carrega pelo email do usuário.
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
		Usuario usuario = usuarioOptional.orElseThrow(() -> new UsernameNotFoundException("Usuário e/ou senha incorretos"));
		// Retorna email, senhas e permissões.
		
		// Retornar UsuarioSistema para ser recuperado no CustomTokenEnhancer
		return new UsuarioSistema(usuario, getPermissoes(usuario));
	}

	private Collection<? extends GrantedAuthority> getPermissoes(Usuario usuario) {
		// HashSet pode ser usada como a melhor opção para operações de pesquisa, pois seu desempenho é mais 
		//rápido.Ele não mantém nenhum pedido de inserção, pois armazena os dados com base no valor do código
		// hash. HashSet permite armazenar valores nulos.HashSet permite armazenar valores nulos.
		// Não está sincronizado.
		// Aqui abaixo está pegando a as permissões , adicionando e retronando a descrição.
		Set<SimpleGrantedAuthority> authorities = new HashSet<>();
		usuario.getPermissoes().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getDescricao().toUpperCase())));
		return authorities;
	}

}
