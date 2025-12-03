package trabalho.model;

public class Revista extends Obra {
    private Integer id_obra;
    private int numeroVolume;
    private int numero_edicao;
    private int ano;
    private String periodicidade;

    // Construtores
    public Revista() {
        super();
    }

    public Revista(Integer id, String titulo, String ano_Publicacao) {
        super();
        this.setId(id);
        this.setTitulo(titulo);
        this.setAno_Publicacao(ano_Publicacao);
        this.setTipo_Obra("Revista");
    }

    // Getters e Setters específicos da Revista
    public Integer getId_obra() {
        return id_obra;
    }

    public void setId_obra(Integer id_obra) {
        this.id_obra = id_obra;
    }

    public int getNumeroVolume() {
        return numeroVolume;
    }

    public void setNumeroVolume(int numeroVolume) {
        this.numeroVolume = numeroVolume;
        // Atualiza também o volume da classe pai como String
        super.setVolume(String.valueOf(numeroVolume));
    }

    public int getNumero_edicao() {
        return numero_edicao;
    }

    public void setNumero_edicao(int numero_edicao) {
        this.numero_edicao = numero_edicao;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public String getPeriodicidade() {
        return periodicidade;
    }

    public void setPeriodicidade(String periodicidade) {
        this.periodicidade = periodicidade;
    }

    @Override
    public String toString() {
        return "Revista{" +
                "id_obra=" + id_obra +
                ", numeroVolume=" + numeroVolume +
                ", numero_edicao=" + numero_edicao +
                ", ano=" + ano +
                ", periodicidade='" + periodicidade + '\'' +
                ", " + super.toString() +
                '}';
    }
}