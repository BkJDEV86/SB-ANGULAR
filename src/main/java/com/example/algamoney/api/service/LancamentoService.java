package com.example.algamoney.api.service;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.algamoney.api.dto.LancamentoEstatisticaPessoa;
import com.example.algamoney.api.mail.Mailer;
import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.model.Usuario;
import com.example.algamoney.api.repository.LancamentoRepository;
import com.example.algamoney.api.repository.PessoaRepository;
import com.example.algamoney.api.repository.UsuarioRepository;
import com.example.algamoney.api.service.exception.PessoaInexistenteOuInativaException;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

// Classe responsável pelas regras de negócio.
@Service
public class LancamentoService {
	
	private static final String DESTINATARIOS = "ROLE_PESQUISAR_LANCAMENTO";
	
	private static final Logger logger = LoggerFactory.getLogger(LancamentoService.class);
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private Mailer mailer;
	
	// Aqui é o agendamento mas precisamos configurar o agendamento...
	//@Scheduled(fixedDelay = 1000*2) extending the usual UN*X definition to include triggerson
	//the second, minute, hour, day of month, month, and day of week. Da Direita para a esquerda todo dia as 6 da manha
	// esse método será executado!
	@Scheduled(cron = "0 0 6 * * *")
	//@Scheduled(fixedDelay = 1000 * 60 * 30)
	public void avisarSobreLancamentosVencidos() {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Preparando envio de "
					+ "e-mails de aviso de lançamentos vencidos.");
		}
		
		
		List<Lancamento> vencidos = lancamentoRepository
				.findByDataVencimentoLessThanEqualAndDataPagamentoIsNull(LocalDate.now());
		
		if (vencidos.isEmpty()) {
			logger.info("Sem lançamentos vencidos para aviso.");
			return;
		}
		
		logger.info("Existem {} lançamentos vencidos.", vencidos.size());

		List<Usuario> destinatarios = usuarioRepository
				.findByPermissoesDescricao(DESTINATARIOS);

		if (destinatarios.isEmpty()) {
			logger.warn("Existem lançamentos vencidos, mas o "
					+ "sistema não encontrou destinatários.");
			return;
		}
		
		
		mailer.avisarSobreLancamentosVencidos(vencidos, destinatarios);
		
		logger.info("Envio de e-mail de aviso concluído.");
	}
	
	
	
	public Lancamento atualizar(Long codigo, Lancamento lancamento) {
		 Lancamento lancamentoSalvo = buscarPessoaPeloCodigo(codigo);
		 if (!lancamento.getPessoa().equals(lancamentoSalvo.getPessoa())) {
				validarPessoa(lancamento);
			}
		 // Aqui é que é esperado pelo menos um recurso
	          // Aqui abaixo vamos pegar a pessoa passada na requisição do postmam e salva-la no banco de dados
			  // através de pessoasalva. Tiramos o código aqui pois ele vem pela URL não passando o código na atualização.
			  BeanUtils.copyProperties(lancamento, lancamentoSalvo, "codigo");
			  return this.lancamentoRepository.save(lancamentoSalvo);
	}

	private void validarPessoa(Lancamento lancamento) {
		Optional<Pessoa> pessoa = null;
		if (lancamento.getPessoa().getCodigo() != null) {
			pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		}

		if (pessoa.isEmpty() || pessoa.get().isInativo()) {
			throw new PessoaInexistenteOuInativaException();
		}
	}

	


//	public void atualizarPropriedadeAtivo(Long codigo, Boolean ativo) {
//		 Lancamento lancamentoSalvo = buscarPessoaPeloCodigo(codigo);
//		 lancamentoSalvo.setAtivo(ativo);
//		 lancamentoRepository.save(lancamentoRepository);	
		
//	}
	
	private Lancamento buscarPessoaPeloCodigo(Long codigo) {
		Lancamento lancamentoSalvo = this.lancamentoRepository.findById(codigo)
			      .orElseThrow(() -> new EmptyResultDataAccessException(1));
		return lancamentoSalvo;
	}
	
	// Quem chamar esse método vai ter que tratar ou relnaçar essa exceção!!!
	public byte[] relatorioPorPessoa(LocalDate inicio, LocalDate fim) throws Exception {
		List<LancamentoEstatisticaPessoa> dados = lancamentoRepository.porPessoa(inicio, fim);
		
		// Abaixo eu importei Date.sql que extende de java util date pois no jaspersoft irá aparecer Date.util 
		Map<String, Object> parametros = new HashMap<>();
		parametros.put("DT_INICIO", Date.valueOf(inicio));
		parametros.put("DT_FIM", Date.valueOf(fim));
		// Por padrão o Jaspersoft passa o local com formato amercicano e precisamos do Brasileiro
		parametros.put("REPORT_LOCALE", new Locale("pt", "BR"));
		
		//Ler o arquivo do jaspersoft pegando o caminho da arquivo
		InputStream inputStream = this.getClass().getResourceAsStream("/relatorios/lancamentos-por-pessoa.jasper");
		
		// 3 parâmetros abaixo o inputstream do relatório, os parâmetros do Map e os dados da lista!!!
		// A classe JasperPrint Representa um documento de página orientado que pode ser visto, printado ou exportado
		// para outro formato.
		JasperPrint jasperPrint = JasperFillManager.fillReport(inputStream, parametros, new JRBeanCollectionDataSource(dados));
		
		// Retorna os bytes do Relatório. Os nossos recursos vao retornar os bytes na requisição
		return JasperExportManager.exportReportToPdf(jasperPrint);
		
	}





	public Lancamento salvar(Lancamento lancamento) {
		Optional<Pessoa> pessoa = pessoaRepository.findById(lancamento.getPessoa().getCodigo());
		if (pessoa.isEmpty() || pessoa.get().isInativo()) {
			throw new PessoaInexistenteOuInativaException();
		}

		return lancamentoRepository.save(lancamento);
	}
	
	private Lancamento buscarLancamentoExistente(Long codigo) {
		/* 		Optional<Lancamento> lancamentoSalvo = lancamentoRepository.findById(codigo);
				if (lancamentoSalvo.isEmpty()) {
					throw new IllegalArgumentException();
				} */
				return lancamentoRepository.findById(codigo).orElseThrow(() -> new IllegalArgumentException());
			}	
	

}
