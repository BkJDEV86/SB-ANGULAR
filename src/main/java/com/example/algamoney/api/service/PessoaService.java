package com.example.algamoney.api.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.example.algamoney.api.model.Pessoa;
import com.example.algamoney.api.repository.PessoaRepository;

// Classe responsável pelas regras de negócio.
@Service
public class PessoaService {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	public Pessoa salvar(Pessoa pessoa) {
		pessoa.getContatos().forEach(c -> c.setPessoa(pessoa));
		return pessoaRepository.save(pessoa);
	}
	
	
	public Pessoa atualizar(Long codigo, Pessoa pessoa) {
		 Pessoa pessoaSalva = buscarPessoaPeloCodigo(codigo);// Aqui é que é esperado pelo menos um recurso
		 
		    pessoaSalva.getContatos().clear();
			pessoaSalva.getContatos().addAll(pessoa.getContatos());
			pessoaSalva.getContatos().forEach(c -> c.setPessoa(pessoaSalva));

			// como já esta adicionado a pessoasalva em contatos na linha acima temos que ignorá-la abaixo
			BeanUtils.copyProperties(pessoa, pessoaSalva, "codigo", "contatos");
	          // Aqui acima vamos pegar a pessoa passada na requisição do postmam e salva-la no banco de dados
			  // através de pessoasalva. Tiramos o código aqui pois ele vem pela URL não passando o código na atualização.
			 
			  return this.pessoaRepository.save(pessoaSalva);
	}


	


	public void atualizarPropriedadeAtivo(Long codigo, Boolean ativo) {
		 Pessoa pessoaSalva = buscarPessoaPeloCodigo(codigo);
		 pessoaSalva.setAtivo(ativo);
		 pessoaRepository.save(pessoaSalva);	
		
	}
	
	public Pessoa buscarPessoaPeloCodigo(Long codigo) {
		Pessoa pessoaSalva = this.pessoaRepository.findById(codigo)
			      .orElseThrow(() -> new EmptyResultDataAccessException(1));
		return pessoaSalva;
	}
	
	

}
