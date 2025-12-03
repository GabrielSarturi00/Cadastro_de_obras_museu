package trabalho.model;

import java.time.LocalDate;

public class Jornal extends Obra {
    private LocalDate dataEdicao;
    private Integer numeroEdicao;
    private String cidade;

    public Jornal() {

    }

    public Jornal(Integer id, String titulo, String tipo_Obra, String ano_Publicacao, LocalDate dataEdicao, Integer numeroEdicao, String cidade) {
        this.dataEdicao = dataEdicao;
        this.numeroEdicao = numeroEdicao;
        this.cidade = cidade;
    }

    public LocalDate getDataEdicao() {
        return dataEdicao;
    }

    public void setDataEdicao(LocalDate dataEdicao) {
        this.dataEdicao = dataEdicao;
    }

    public Integer getNumeroEdicao() {
        return numeroEdicao;
    }

    public void setNumeroEdicao(Integer numeroEdicao) {
        this.numeroEdicao = numeroEdicao;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
}
