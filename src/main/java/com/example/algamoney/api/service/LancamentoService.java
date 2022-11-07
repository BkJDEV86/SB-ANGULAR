package com.example.algamoney.api.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.LancamentoRepository;
import com.example.algamoney.api.repository.PessoaRepository;
import com.example.algamoney.api.service.exception.PessoaInexistenteOuInativaException;

// Classe responsável pelas regras de negócio.
@Service
public class LancamentoService {
	
	@Autowired
	private LancamentoRepository lancamentoRepository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	
	
	
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
