package trabalho.model;

public class Obra  {
    private Integer id;
    private String titulo;
    private String tipo_Obra;
    private String ano_Publicacao;
    private String autor;
    private String editora;
    private String volume;
    private String edicao;
    private String isbn;
    private String chamada;
    private String numero; // Para revista.numero e jornal.numero_edicao

    // Construtores
    public Obra() {
    }

    public Obra(Integer id, String titulo, String tipo_Obra, String ano_Publicacao) {
        this.id = id;
        this.titulo = titulo;
        this.tipo_Obra = tipo_Obra;
        this.ano_Publicacao = ano_Publicacao;
    }

    public Obra(Integer id, String titulo, String tipo_Obra, String ano_Publicacao,
                String autor, String editora) {
        this.id = id;
        this.titulo = titulo;
        this.tipo_Obra = tipo_Obra;
        this.ano_Publicacao = ano_Publicacao;
        this.autor = autor;
        this.editora = editora;
    }

    public Obra(Integer id, String titulo, String tipo_Obra, String ano_Publicacao,
                String autor, String editora, String chamada, String numero) {
        this.id = id;
        this.titulo = titulo;
        this.tipo_Obra = tipo_Obra;
        this.ano_Publicacao = ano_Publicacao;
        this.autor = autor;
        this.editora = editora;
        this.chamada = chamada;
        this.numero = numero;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo_Obra() {
        return tipo_Obra;
    }

    public void setTipo_Obra(String tipo_Obra) {
        this.tipo_Obra = tipo_Obra;
    }

    public String getAno_Publicacao() {
        return ano_Publicacao;
    }

    public void setAno_Publicacao(String ano_Publicacao) {
        this.ano_Publicacao = ano_Publicacao;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getEdicao() {
        return edicao;
    }

    public void setEdicao(String edicao) {
        this.edicao = edicao;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getChamada() {
        return chamada;
    }

    public void setChamada(String chamada) {
        this.chamada = chamada;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "Obra{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", tipo_Obra='" + tipo_Obra + '\'' +
                ", ano_Publicacao='" + ano_Publicacao + '\'' +
                ", autor='" + autor + '\'' +
                ", editora='" + editora + '\'' +
                ", volume='" + volume + '\'' +
                ", edicao='" + edicao + '\'' +
                ", isbn='" + isbn + '\'' +
                ", chamada='" + chamada + '\'' +
                ", numero='" + numero + '\'' +
                '}';
    }
}