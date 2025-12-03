package trabalho.controller;

import trabalho.dao.ObraDAO;
import trabalho.model.Obra;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

/**
 * Controller da tela de obras.
 *
 * Responsabilidades:
 * - Inicializar os componentes da UI (ComboBoxes, TableView, colunas).
 * - Preencher/limpar o formulário.
 * - Validar dados antes de salvar.
 * - Chamar o DAO para listar, inserir, atualizar e excluir obras.
 * - Exibir mensagens de erro/ sucesso ao usuário.
 */
public class ObraController {

    // ======== Controles da interface (conectados via FXML) ========
    @FXML private TextField txtId;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtVolume;
    @FXML private TextField txtEdicao;
    @FXML private TextField txtEditora;
    @FXML private TextField txtIsbn;
    @FXML private TextField txtChamada;
    @FXML private ComboBox<String> cbTipoObra;
    @FXML private ComboBox<String> cbAnoPublicacao;

    @FXML private TableView<Obra> table;
    @FXML private TableColumn<Obra, Number> colId;
    @FXML private TableColumn<Obra, String> colTitulo;
    @FXML private TableColumn<Obra, String> colAutor;
    @FXML private TableColumn<Obra, String> colTipoObra;
    @FXML private TableColumn<Obra, String> colAnoPublicacao;
    @FXML private TableColumn<Obra, String> colVolume;
    @FXML private TableColumn<Obra, String> colEdicao;
    @FXML private TableColumn<Obra, String> colEditora;
    @FXML private TableColumn<Obra, String> colIsbn;
    @FXML private TableColumn<Obra, String> colChamada;

    // DAO para acessar o banco de dados (padrão: um DAO por entidade)
    private final ObraDAO dao = new ObraDAO();

    // Lista observável que alimenta a TableView (facilita atualização automática da tabela)
    private final ObservableList<Obra> dados = FXCollections.observableArrayList();

    /**
     * Método chamado automaticamente pelo JavaFX após a injeção dos componentes FXML.
     * Aqui configuramos ComboBoxes, colunas da tabela, listeners e carregamos os dados.
     */
    @FXML
    public void initialize() {
        // ---------- Inicializa ComboBox de tipo de obra ----------
        cbTipoObra.setItems(FXCollections.observableArrayList(
                "Livro", "Livro Online", "Revista", "Jornal"
        ));

        // ---------- Inicializa ComboBox de anos (de 2025 até 1500) ----------
        ObservableList<String> anos = FXCollections.observableArrayList();
        for (int ano = 2025; ano >= 1500; ano--) {
            anos.add(String.valueOf(ano));
        }
        cbAnoPublicacao.setItems(anos);
        cbAnoPublicacao.setEditable(true); // permite digitar um ano não listado

        // ---------- Configuração das colunas da tabela ----------
        // Cada coluna recebe um callback que transforma o modelo Obra em propriedades exibíveis.
        colId.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colTitulo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTitulo()));
        colAutor.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getAutor()));
        colTipoObra.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getTipo_Obra()));
        colAnoPublicacao.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getAno_Publicacao()));

        // Para campos opcionais (p. ex. volume, edicao, isbn) garantimos não mostrar "null"
        colVolume.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getVolume() != null ? c.getValue().getVolume() : ""
                ));
        colEdicao.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getEdicao() != null ? c.getValue().getEdicao() : ""
                ));
        // colEditora teve duas configurações no original — mantemos apenas a correta abaixo
        colEditora.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEditora()));
        colIsbn.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getIsbn() != null ? c.getValue().getIsbn() : ""
                ));
        colChamada.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getChamada() != null ? c.getValue().getChamada() : ""
                ));

        // ---------- Liga a lista observável à tabela ----------
        table.setItems(dados);

        // Quando o usuário seleciona uma linha, preenche o formulário com os dados da obra
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> preencherFormulario(sel)
        );

        // Carrega os dados do banco inicialmente
        recarregarTabela();
    }

    /**
     * Preenche (ou limpa) o formulário com os dados de uma Obra.
     * @param obra Obra selecionada na tabela; se null, limpa o formulário.
     */
    private void preencherFormulario(Obra obra) {
        if (obra == null) {
            // Limpa todos os campos e seleção dos comboboxes
            txtId.clear();
            txtTitulo.clear();
            txtAutor.clear();
            txtVolume.clear();
            txtEdicao.clear();
            txtEditora.clear();
            txtIsbn.clear();
            txtChamada.clear();
            cbTipoObra.getSelectionModel().clearSelection();
            cbAnoPublicacao.getSelectionModel().clearSelection();
            cbAnoPublicacao.setValue(null);
            return;
        }

        // Seta os campos com os dados da obra (tratando possíveis nulls)
        txtId.setText(String.valueOf(obra.getId()));
        txtTitulo.setText(obra.getTitulo());
        txtAutor.setText(obra.getAutor());
        txtVolume.setText(obra.getVolume() != null ? obra.getVolume() : "");
        txtEdicao.setText(obra.getEdicao() != null ? obra.getEdicao() : "");
        txtEditora.setText(obra.getEditora());
        txtIsbn.setText(obra.getIsbn() != null ? obra.getIsbn() : "");
        txtChamada.setText(obra.getChamada() != null ? obra.getChamada() : "");
        cbTipoObra.setValue(obra.getTipo_Obra());
        cbAnoPublicacao.setValue(obra.getAno_Publicacao());
    }

    // ---------- Ações dos botões (ligadas no FXML) ----------

    /** Ao clicar em "Novo": limpa seleção e formulário para inserir nova obra. */
    @FXML
    private void onNovo() {
        table.getSelectionModel().clearSelection();
        preencherFormulario(null);
    }

    /**
     * Ao clicar em "Salvar":
     * - Lê valores do formulário
     * - Valida
     * - Cria objeto Obra
     * - Chama DAO para inserir ou atualizar
     */
    @FXML
    private void onSalvar() {
        try {
            // Lê e normaliza os campos (trim para evitar espaços)
            String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
            String autor = txtAutor.getText() != null ? txtAutor.getText().trim() : "";
            String tipoObra = cbTipoObra.getValue();
            String anoPublicacao = cbAnoPublicacao.getEditor().getText() != null
                    ? cbAnoPublicacao.getEditor().getText().trim()
                    : cbAnoPublicacao.getValue();
            String volume = txtVolume.getText() != null ? txtVolume.getText().trim() : null;
            String edicao = txtEdicao.getText() != null ? txtEdicao.getText().trim() : null;
            String editora = txtEditora.getText() != null ? txtEditora.getText().trim() : "";
            String isbn = txtIsbn.getText() != null ? txtIsbn.getText().trim() : null;
            String chamada = txtChamada.getText() != null ? txtChamada.getText().trim() : "";

            // Valida campos obrigatórios e formato do ano
            validar(titulo, autor, tipoObra, anoPublicacao, editora, chamada);

            // Constrói o objeto Obra a partir dos campos
            String idStr = txtId.getText();
            Obra obra = new Obra();

            if (idStr != null && !idStr.isBlank()) {
                // Se id preenchido, é atualização
                obra.setId(Integer.parseInt(idStr));
            }

            obra.setTitulo(titulo);
            obra.setAutor(autor);
            obra.setTipo_Obra(tipoObra);
            obra.setAno_Publicacao(anoPublicacao);

            // Volume: se vazio, guardamos null no objeto (assim o banco fica limpo)
            if (volume != null && !volume.isEmpty()) {
                obra.setVolume(volume);
                System.out.println("DEBUG: Volume definido: " + volume);
            } else {
                obra.setVolume(null);
                System.out.println("DEBUG: Volume é null ou vazio");
            }

            // Edicao também pode ser null se vazio
            obra.setEdicao(edicao != null && !edicao.isEmpty() ? edicao : null);
            obra.setEditora(editora);
            obra.setIsbn(isbn != null && !isbn.isEmpty() ? isbn : null);
            obra.setChamada(chamada);

            // Decide inserir ou atualizar com base na presença do id
            if (obra.getId() == null) {
                dao.inserir(obra);   // insere nova obra
            } else {
                dao.atualizar(obra); // atualiza obra existente
            }

            // Atualiza tabela, limpa formulário e mostra mensagem de sucesso
            recarregarTabela();
            onNovo();
            showInfo("Sucesso", "Obra salva com sucesso.");
        } catch (IllegalArgumentException e) {
            // Erros de validação mostrados ao usuário
            showError("Validação", e.getMessage());
        } catch (SQLException e) {
            // Erros do banco: mostrar mensagem e imprimir stacktrace para debug
            showError("Banco de dados", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ao clicar em "Excluir":
     * - Confirmação com o usuário
     * - Chamada ao DAO para remover a obra por id
     */
    @FXML
    private void onExcluir() {
        try {
            if (txtId.getText() == null || txtId.getText().isBlank()) {
                showError("Atenção", "Selecione uma obra para excluir.");
                return;
            }
            int id = Integer.parseInt(txtId.getText());

            // Caixa de confirmação (modal)
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Excluir a obra selecionada?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirmação");
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                dao.excluir(id);
                recarregarTabela();
                onNovo();
                showInfo("Sucesso", "Obra excluída com sucesso.");
            }
        } catch (SQLException e) {
            showError("Banco de dados", e.getMessage());
            e.printStackTrace();
        }
    }

    /** Recarrega os dados da tabela chamando o DAO e substitui o conteúdo da lista observável. */
    private void recarregarTabela() {
        try {
            dados.setAll(dao.listar()); // lista() retorna List<Obra>
        } catch (SQLException e) {
            showError("Banco de dados", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valida campos obrigatórios:
     * - título, autor, tipo da obra, ano, editora e chamada são obrigatórios
     * - ano deve ser numérico e estar entre 1500 e 2100
     * Lança IllegalArgumentException em caso de erro (tratado em onSalvar()).
     */
    private void validar(String titulo, String autor, String tipoObra,
                         String anoPublicacao, String editora, String chamada) {
        if (titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório.");
        }
        if (autor.isBlank()) {
            throw new IllegalArgumentException("Autor é obrigatório.");
        }
        if (tipoObra == null) {
            throw new IllegalArgumentException("Tipo da obra é obrigatório.");
        }
        if (!tipoObra.equals("Livro") && !tipoObra.equals("Livro Online")
                && !tipoObra.equals("Revista") && !tipoObra.equals("Jornal")) {
            throw new IllegalArgumentException(
                    "Tipo da obra inválido. Use: 'Livro', 'Livro Online', 'Revista' ou 'Jornal'.");
        }
        if (anoPublicacao == null || anoPublicacao.isBlank()) {
            throw new IllegalArgumentException("Ano de publicação é obrigatório.");
        }
        try {
            int ano = Integer.parseInt(anoPublicacao);
            if (ano < 1500 || ano > 2100) {
                throw new IllegalArgumentException("Ano deve estar entre 1500 e 2100.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ano de publicação deve ser um número válido.");
        }
        if (editora.isBlank()) {
            throw new IllegalArgumentException("Editora é obrigatória.");
        }
        if (chamada.isBlank()) {
            throw new IllegalArgumentException("Chamada é obrigatória.");
        }
    }

    // ======= Métodos utilitários para mostrar diálogos ao usuário =======

    /** Mostra mensagem de erro (AlertType.ERROR) com título e conteúdo. */
    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }

    /** Mostra mensagem informativa (AlertType.INFORMATION). */
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
