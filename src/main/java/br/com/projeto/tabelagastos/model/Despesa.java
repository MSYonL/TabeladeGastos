package br.com.projeto.tabelagastos.model;

import java.util.Objects;

public class Despesa implements Comparable<Despesa> {
    private String conta;
    private boolean parcelada;
    private String numeroParcela;
    private double valor;
    private boolean pago;
    private String mes;
    private String ano;
    private String vencimento;

    public Despesa(String conta, boolean parcelada, String numeroParcela, double valor,
                   boolean pago, String mes, String ano, String vencimento) {
        this.conta = conta;
        this.parcelada = parcelada;
        this.numeroParcela = numeroParcela;
        this.valor = valor;
        this.pago = pago;
        this.mes = mes;
        this.ano = ano;
        this.vencimento = vencimento;
    }

    // Getters
    public String getConta() { return conta; }
    public boolean isParcelada() { return parcelada; }
    public String getNumeroParcela() { return numeroParcela; }
    public double getValor() { return valor; }
    public boolean isPago() { return pago; }
    public String getMes() { return mes; }
    public String getAno() { return ano; }
    public String getVencimento() { return vencimento; }

    // toString para facilitar debug ou exibição
    @Override
    public String toString() {
        return String.format("Despesa[conta=%s, valor=%.2f, pago=%s, vencimento=%s]",
                conta, valor, pago ? "Sim" : "Não", vencimento);
    }

    // equals e hashCode para comparação e uso em coleções
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Despesa)) return false;
        Despesa despesa = (Despesa) o;
        return Double.compare(despesa.valor, valor) == 0 &&
               parcelada == despesa.parcelada &&
               pago == despesa.pago &&
               Objects.equals(conta, despesa.conta) &&
               Objects.equals(numeroParcela, despesa.numeroParcela) &&
               Objects.equals(mes, despesa.mes) &&
               Objects.equals(ano, despesa.ano) &&
               Objects.equals(vencimento, despesa.vencimento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conta, parcelada, numeroParcela, valor, pago, mes, ano, vencimento);
    }

    // Ordenação por vencimento (opcional)
    @Override
    public int compareTo(Despesa outra) {
        return this.vencimento.compareTo(outra.vencimento);
    }
}