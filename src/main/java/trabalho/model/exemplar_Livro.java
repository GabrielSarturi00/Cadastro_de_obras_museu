package trabalho.model;

public class exemplar_Livro extends Obra {
    private Integer idExemplar;
    private Integer idObra;
    private String codigoInterno;
    private String situacao;

    // Construtores
    public exemplar_Livro() {
        super();
        this.setTipo_Obra("Exemplar");
    }

    public exemplar_Livro(Integer id, String titulo, String ano_Publicacao) {
        super();
        this.setId(id);
        this.setTitulo(titulo);
        this.setAno_Publicacao(ano_Publicacao);
        this.setTipo_Obra("Exemplar");
    }

    // Getters e Setters
    public Integer getIdExemplar() {
        return idExemplar;
    }

    public void setIdExemplar(Integer idExemplar) {
        this.idExemplar = idExemplar;
    }

    public Integer getIdObra() {
        return idObra;
    }

    public void setIdObra(Integer idObra) {
        this.idObra = idObra;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    @Override
    public String toString() {
        return "exemplar_Livro{" +
                "idExemplar=" + idExemplar +
                ", idObra=" + idObra +
                ", codigoInterno='" + codigoInterno + '\'' +
                ", situacao='" + situacao + '\'' +
                ", " + super.toString() +
                '}';
    }
}