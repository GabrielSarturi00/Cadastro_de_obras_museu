package trabalho.model;

public class Livro extends Obra {
    private Integer numPaginas;

    // Construtores
    public Livro() {
        super();
        this.setTipo_Obra("Livro");
    }

    public Livro(Integer id, String titulo, String ano_Publicacao) {
        super();
        this.setId(id);
        this.setTitulo(titulo);
        this.setAno_Publicacao(ano_Publicacao);
        this.setTipo_Obra("Livro");
    }

    public Livro(Integer id, String titulo, String ano_Publicacao, String isbn) {
        super();
        this.setId(id);
        this.setTitulo(titulo);
        this.setAno_Publicacao(ano_Publicacao);
        this.setIsbn(isbn);
        this.setTipo_Obra("Livro");
    }

    // Getters e Setters
    public Integer getNumPaginas() {
        return numPaginas;
    }

    public void setNumPaginas(Integer numPaginas) {
        this.numPaginas = numPaginas;
    }

    @Override
    public String toString() {
        return "Livro{" +
                "numPaginas=" + numPaginas +
                ", isbn='" + getIsbn() + '\'' +
                ", " + super.toString() +
                '}';
    }
}