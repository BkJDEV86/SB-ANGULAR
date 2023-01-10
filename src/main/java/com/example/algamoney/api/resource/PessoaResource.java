package com.example.algamoney.api.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.PessoaRepository;
import com.example.algamoney.api.service.PessoaService;

@RestController
@RequestMapping("/pessoas")
public class PessoaResource {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private PessoaService pessoaService;
	
	@Autowired
	// publicador de eventos de aplicação.
	private ApplicationEventPublisher publisher;
	
	//@GetMapping
//	public List<Pessoa> listar(){
	//	return pessoaRepository.findAll();
//	}
	
	@PostMapping // Aqui abaixo vai permitir que quando criado uma categoria no json ela vai virar objeto
	
	// O servelet é um pequeno servidor que responsável por gerenciar requisições https se comunicando
	// com o cliente
	// O responseEntity retorna uma um objeto(categoria)
	// É necessário validar a categoria  para que as validações inseridas sejam reconhecidas e executadas
	// A aplicação do Spring Validation é bastante simples e prática e precisa ser reconhecida.
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA') and hasAuthority('SCOPE_write')")
	public ResponseEntity<Pessoa> criar(@Valid @RequestBody Pessoa pessoa, HttpServletResponse response){
		// Aqui é necessario instanciar o objeto categoriaSalva pois vamos precisar do id.
		
		Pessoa pessoaSalva = pessoaService.salvar(pessoa);
		
		// Aqui é passaod o this que é o próprio objeto da classe, a resposta e o código da pessoa salva
		publisher.publishEvent(new RecursoCriadoEvent(this, response, pessoaSalva.getCodigo()));
		
		// Abaixo pegamos a partir da requisição atual que foi pra /categorias adicionar o path código
		// e adicionar o código na URI setando o headerLocation com essa URI.
		
		//return ResponseEntity.created(uri).body(pessoaSalva	);
		
		// não temos mais a uri devemos passar o 
		return ResponseEntity.status(HttpStatus.CREATED).body(pessoaSalva	);
		
	}
	
	
	
	
	
	@GetMapping("/{codigo}")
	// O @PathVariable é pq o id vai variar. 
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_PESSOA') and hasAuthority('SCOPE_read')")
	public ResponseEntity<Pessoa> buscarPeloCodigo(@PathVariable Long codigo) {
	Optional<Pessoa> pessoa = this.pessoaRepository.findById(codigo);
	// operador ternário para se achar ok, do contrário retorna not found(404).
	return pessoa.isPresent() ? 
	        ResponseEntity.ok(pessoa.get()) : ResponseEntity.notFound().build();
	}
	
	
	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)// Não vai retornar conteúdo.
	@PreAuthorize("hasAuthority('ROLE_REMOVER_PESSOA') and hasAuthority('SCOPE_write')")
	public void remover(@PathVariable Long codigo) {
		this.pessoaRepository.deleteById(codigo);
	}
	
	
	@PutMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA') and hasAuthority('SCOPE_write')")
	public Pessoa atualizar(@PathVariable Long codigo, @Valid @RequestBody Pessoa pessoa) {

		 Pessoa pessoaSalva = pessoaService.atualizar(codigo, pessoa);

		return pessoaRepository.save(pessoaSalva);
		}
	
	@PutMapping("/{codigo}/ativo")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_PESSOA') and hasAuthority('SCOPE_write')")
	public void atualizarPropriedadeAtivo(@PathVariable Long codigo, @RequestBody Boolean ativo) {
		pessoaService.atualizarPropriedadeAtivo(codigo,ativo);
	}
	
	@GetMapping
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_PESSOA')")
	public Page<Pessoa> pesquisar(@RequestParam(required = false, defaultValue = "") String nome, Pageable pageable) {
	    return pessoaRepository.findByNomeContaining(nome, pageable);
	}
	
	
	

}


















