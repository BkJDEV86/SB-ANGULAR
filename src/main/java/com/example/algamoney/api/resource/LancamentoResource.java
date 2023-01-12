package com.example.algamoney.api.resource;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.example.algamoney.api.dto.LancamentoEstatisticaDia;
import com.example.algamoney.api.event.RecursoCriadoEvent;
import com.example.algamoney.api.exceptionhandler.AlgamoneyExceptionHandler.Erro;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.repository.LancamentoRepository;
import com.example.algamoney.api.repository.filter.LancamentoFilter;
import com.example.algamoney.api.repository.projection.ResumoLancamento;
import com.example.algamoney.api.service.LancamentoService;
import com.example.algamoney.api.service.exception.PessoaInexistenteOuInativaException;

@RestController
@RequestMapping("/lancamentos")
public class LancamentoResource {
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private LancamentoService lancamentoService;
	
	@Autowired
	// publicador de eventos de aplicação.
	private ApplicationEventPublisher publisher;
	
	@Autowired
	private MessageSource messageSource;
	
	//OutputStream é uma classe abstrata de Byte Stream que descreve a saída do stream e é usada para gravar dados em um arquivo, 
	//imagem, áudio, etc
	//É uma classe abstrata que oferece a funcionalidade básica para a leitura de um byte ou de uma sequência de bytes a partir
	//de alguma fonte.
	@PostMapping("/anexo")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO') and hasAuthority('SCOPE_write')")
	public String uploadAnexo(@RequestParam MultipartFile anexo) throws IOException {
		OutputStream out = new FileOutputStream(
				"/Users/Dell/Desktop/anexo--"   + anexo.getOriginalFilename());
		out.write(anexo.getBytes());
		out.close();
		return "ok";
	}
	
	
	// Devido á nova stack de segurança do Spring Security a expressão "#oauth2.hasScope" não é mais utilizada. No seu lugar vamos
	// utilizar a expressão "hasAuthority", informando um escopo específico, exemplo: hasAuthority('SCOPE_read').
	
    // Aqui já estou retornando o pdf por isso /relatorios e não estatisticas
	@GetMapping("/relatorios/por-pessoa")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')")
	public ResponseEntity<byte[]> relatorioPorPessoa(
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate inicio,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fim) throws Exception {
		byte[] relatorio = lancamentoService.relatorioPorPessoa(inicio, fim);

		// No headers Um cabeçalho de solicitação é composto por seu nome case-insensitive (não diferencia letras maiúsculas e
		// minúsculas), seguido por dois pontos ':' e pelo seu valor (sem quebras de linha)
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
				.body(relatorio);
	}
	
	@GetMapping("/estatisticas/por-dia")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')")
	public List<LancamentoEstatisticaDia> porDia() {
		return this.lancamentoRepository.porDia(LocalDate.now());
	}
	
	// Aqui foi feita uma alteração para poder visualizar os dados corretamente...
	@GetMapping("/estatisticas/por-categoria")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')")
	public List<LancamentoEstatisticaCategoria> porCategoria(
			@RequestParam(name = "mesReferencia", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") 
			LocalDate mesReferencia) {
		return this.lancamentoRepository.porCategoria(mesReferencia);
	}
	
	@GetMapping
	// Pageable é para colocar paginação.
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')")
	public Page<Lancamento> pesquisar(LancamentoFilter lancamentoFilter, Pageable pageable){
		return lancamentoRepository.filtrar(lancamentoFilter, pageable);
	}
	
	@GetMapping(params = "resumo")
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')")
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		return lancamentoRepository.resumir(lancamentoFilter, pageable);
	}
	
	@PostMapping // Aqui abaixo vai permitir que quando criado uma categoria no json ela vai virar objeto
	
	// O servelet é um pequeno servidor que responsável por gerenciar requisições https se comunicando
	// com o cliente
	// O responseEntity retorna uma um objeto(categoria)
	// É necessário validar a categoria  para que as validações inseridas sejam reconhecidas e executadas
	// A aplicação do Spring Validation é bastante simples e prática e precisa ser reconhecida.
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO') and hasAuthority('SCOPE_write')")
	public ResponseEntity<Lancamento> criar(@Valid @RequestBody Lancamento lancamento, HttpServletResponse response){
		// Aqui é necessario instanciar o objeto categoriaSalva pois vamos precisar do id.
		
		Lancamento lancamentoSalvo =lancamentoService.salvar	(lancamento);
		
		publisher.publishEvent(new RecursoCriadoEvent(this, response, lancamentoSalvo.getCodigo()));
		// Abaixo pegamos a partir da requisição atual que foi pra /categorias adicionar o path código
		// e adicionar o código na URI setando o headerLocation com essa URI.
		
		
		return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoSalvo);
		
	
		
	}
	
	
	
	
	
	
	@GetMapping("/{codigo}")
	// O @PathVariable é pq o id vai variar. 
	@PreAuthorize("hasAuthority('ROLE_PESQUISAR_LANCAMENTO') and hasAuthority('SCOPE_read')")
	public ResponseEntity<Lancamento> buscarPeloCodigo(@PathVariable Long codigo) {
	Optional<Lancamento> lancamento = this.lancamentoRepository.findById(codigo);
	// operador ternário para se achar ok, do contrário retorna not found(404).
	return lancamento.isPresent() ? 
	        ResponseEntity.ok(lancamento.get()) : ResponseEntity.notFound().build();
	}
	

	
	
	
	// Aqui como é uma exceção especifica pode ser tratada aqui ao invés do ExceptionHandler
	@ExceptionHandler({PessoaInexistenteOuInativaException.class})
	public ResponseEntity<Object> handlePessoaInexistenteOuInativaException(PessoaInexistenteOuInativaException ex){
		String mensagemUsuario = messageSource.getMessage("pessoa.inexistente-ou-inativa", null, LocaleContextHolder.getLocale());
		// senão usar o ternário pegando a causa quando diferente de null vai dar exceção.
		String mensagemDesenvolvedor = ex.toString() ;
		List<Erro> erros = Arrays.asList(new Erro(mensagemUsuario,mensagemDesenvolvedor));
		return ResponseEntity.badRequest().body(erros);
	}
	
	
	
	@DeleteMapping("/{codigo}")
	@ResponseStatus(HttpStatus.NO_CONTENT)// Não vai retornar conteúdo.
	@PreAuthorize("hasAuthority('ROLE_REMOVER_LANCAMENTO') and hasAuthority('SCOPE_write')")
	public void remover(@PathVariable Long codigo) {
		this.lancamentoRepository.deleteById(codigo);
	}
	
	
	
//	@PutMapping("/{codigo}/ativo")
//	@ResponseStatus(HttpStatus.NO_CONTENT)
//	public void atualizarPropriedadeAtivo(@PathVariable Long codigo, @RequestBody Boolean ativo) {
//		lancamentoService.atualizarPropriedadeAtivo(codigo,ativo);
//	}
	
	
	@PutMapping("/{codigo}")
	@PreAuthorize("hasAuthority('ROLE_CADASTRAR_LANCAMENTO')")
	public ResponseEntity<Lancamento> atualizar(@PathVariable Long codigo, @Valid @RequestBody Lancamento lancamento) {
		try {
			Lancamento lancamentoSalvo = lancamentoService.atualizar(codigo, lancamento);
			return ResponseEntity.ok(lancamentoSalvo);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	
	
	
	
	
	
	
	
	
}


















