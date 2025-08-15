package br.com.projeto.tabelagastos.service;

import br.com.projeto.tabelagastos.model.Despesa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DespesaService {
    private final List<Despesa> despesas = new ArrayList<>();

    // Adiciona uma nova despesa
    public void adicionarDespesa(Despesa despesa) {
        if (despesa != null) {
            despesas.add(despesa);
        }
    }

    // Atualiza uma despesa existente
    public void atualizarDespesa(int index, Despesa novaDespesa) {
        if (index >= 0 && index < despesas.size() && novaDespesa != null) {
            despesas.set(index, novaDespesa);
        } else {
            throw new IndexOutOfBoundsException("Índice inválido ou despesa nula: " + index);
        }
    }

    // Remove uma despesa pelo índice
    public void removerDespesa(int index) {
        if (index >= 0 && index < despesas.size()) {
            despesas.remove(index);
        } else {
            throw new IndexOutOfBoundsException("Índice inválido: " + index);
        }
    }

    // Retorna uma cópia imutável da lista de despesas
    public List<Despesa> listarDespesas() {
        return Collections.unmodifiableList(new ArrayList<>(despesas));
    }

    // Calcula o total de despesas de um mês e ano específicos
    public double calcularTotalMes(String mes, String ano) {
        return despesas.stream()
                .filter(d -> d.getMes().equalsIgnoreCase(mes) && d.getAno().equalsIgnoreCase(ano))
                .mapToDouble(Despesa::getValor)
                .sum();
    }

    // Filtra despesas por mês e ano
    public List<Despesa> filtrarPorMesEAno(String mes, String ano) {
        return despesas.stream()
                .filter(d -> d.getMes().equalsIgnoreCase(mes) && d.getAno().equalsIgnoreCase(ano))
                .collect(Collectors.toList());
    }

    // Limpa todas as despesas
    public void limparDespesas() {
        despesas.clear();
    }

    // Retorna o total de despesas pagas
    public double calcularTotalPago() {
        return despesas.stream()
                .filter(Despesa::isPago)
                .mapToDouble(Despesa::getValor)
                .sum();
    }

    // Retorna o total de despesas não pagas
    public double calcularTotalNaoPago() {
        return despesas.stream()
                .filter(d -> !d.isPago())
                .mapToDouble(Despesa::getValor)
                .sum();
    }
}