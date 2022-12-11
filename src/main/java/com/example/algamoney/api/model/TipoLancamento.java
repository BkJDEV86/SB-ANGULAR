package com.example.algamoney.api.model;



/* Foi criada uma propriedade descrição para ser exibida no relatório */
public enum TipoLancamento {

	RECEITA("Receita"), 
	DESPESA("Despesa");

	private final String descricao;

	TipoLancamento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}