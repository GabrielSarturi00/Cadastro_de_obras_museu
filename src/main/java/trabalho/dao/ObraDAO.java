package trabalho.dao;

import trabalho.db.Db;
import trabalho.model.Obra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para a entidade Obra.
 *
 * Responsabilidades principais:
 * - Isolar o código de acesso ao banco de dados (SQL, conexões, transações).
 * - Fornecer métodos para inserir, atualizar, excluir e listar obras.
 * - Tratar transações e rollback em caso de erro.
 *
 * Observação: este DAO trabalha com diversas tabelas relacionadas:
 *   obras, autores, editoras, livros, livros_online, revistas, jornais, obras_autores.
 */
public class ObraDAO {

    /**
     * Insere uma nova obra no banco de dados.
     *
     * Fluxo geral:
     * 1) Abre conexão e inicia transação (autoCommit = false).
     * 2) Insere/obtém ID do autor (tabela autores).
     * 3) Insere/obtém ID da editora (tabela editoras).
     * 4) Insere registro na tabela obras (campos principais).
     * 5) Insere relacionamento obra <-> autor (tabela obras_autores).
     * 6) Insere dados na tabela específica conforme o tipo (livros, revistas, jornais, livros_online).
     * 7) Commit da transação; em caso de erro, rollback.
     */
    public void inserir(Obra obra) throws SQLException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false); // começa transação manual

            // 1. Inserir ou obter ID do autor (evita duplicatas)
            Integer idAutor = inserirOuObterAutor(conn, obra.getAutor());

            // 2. Inserir ou obter ID da editora (evita duplicatas)
            Integer idEditora = inserirOuObterEditora(conn, obra.getEditora());

            // 3. Inserir na tabela obras e obter o id gerado (id_obra)
            final String sqlObra =
                    "INSERT INTO obras (chamada, chamada_local, titulo, edicao, ano_publicacao, id_editora) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            Integer idObra;

            try (PreparedStatement ps = conn.prepareStatement(sqlObra, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, obra.getChamada());
                ps.setString(2, obra.getChamada()); // chamada_local = chamada (mesma informação)
                ps.setString(3, obra.getTitulo());
                // edicao pode ser null se não informado
                ps.setString(4, obra.getEdicao() != null && !obra.getEdicao().isEmpty() ? obra.getEdicao() : null);
                ps.setInt(5, Integer.parseInt(obra.getAno_Publicacao())); // ano é obrigatório e já validado no controller
                ps.setInt(6, idEditora);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idObra = rs.getInt(1);
                        obra.setId(idObra); // atualiza o objeto com o id gerado
                    } else {
                        throw new SQLException("Falha ao obter ID da obra inserida.");
                    }
                }
            }

            // 4. Relacionar obra com autor (tabela de relacionamento)
            final String sqlObraAutor = "INSERT INTO obras_autores (id_obra, id_autor) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlObraAutor)) {
                ps.setInt(1, idObra);
                ps.setInt(2, idAutor);
                ps.executeUpdate();
            }

            // 5. Inserir na tabela específica conforme o tipo da obra
            String tipoObra = obra.getTipo_Obra();

            if ("Livro".equals(tipoObra)) {
                // Tabela livros armazena ISBN para livros físicos
                final String sqlLivro = "INSERT INTO livros (id_obra, isbn) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlLivro)) {
                    ps.setInt(1, idObra);
                    ps.setString(2, obra.getIsbn());
                    ps.executeUpdate();
                }
            } else if ("Livro Online".equals(tipoObra)) {
                // Livros online podem ter menos campos (apenas relacionamento com obra)
                final String sqlLivroOnline = "INSERT INTO livros_online (id_obra) VALUES (?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlLivroOnline)) {
                    ps.setInt(1, idObra);
                    ps.executeUpdate();
                }
            } else if ("Revista".equals(tipoObra)) {
                // Revistas: issn, volume e numero (numero usamos do campo edicao)
                final String sqlRevista = "INSERT INTO revistas (id_obra, issn, volume, numero) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlRevista)) {
                    ps.setInt(1, idObra);
                    ps.setString(2, obra.getIsbn() != null ? obra.getIsbn() : "");

                    // Volume: se vazio insere NULL
                    if (obra.getVolume() != null && !obra.getVolume().isEmpty()) {
                        ps.setString(3, obra.getVolume());
                    } else {
                        ps.setNull(3, java.sql.Types.VARCHAR);
                    }

                    // Numero: usamos edicao como numero da revista
                    if (obra.getEdicao() != null && !obra.getEdicao().isEmpty()) {
                        ps.setString(4, obra.getEdicao());
                    } else {
                        ps.setNull(4, java.sql.Types.VARCHAR);
                    }

                    ps.executeUpdate();
                }
            } else if ("Jornal".equals(tipoObra)) {
                // Jornais: issn e numero_edicao (usamos edicao)
                final String sqlJornal = "INSERT INTO jornais (id_obra, issn, numero_edicao) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlJornal)) {
                    ps.setInt(1, idObra);
                    ps.setString(2, obra.getIsbn()); // ISSN do jornal (pode ser null)
                    ps.setString(3, obra.getEdicao()); // numero_edicao usa campo edicao
                    ps.executeUpdate();
                }
            }

            conn.commit(); // confirma transação
        } catch (SQLException e) {
            // Em caso de erro, tenta rollback para manter integridade
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // repropaga para ser tratado em camadas superiores
        } finally {
            // Garante restauração do autoCommit e fechamento da conexão
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Atualiza uma obra existente.
     *
     * Fluxo:
     * - Verifica id não nulo
     * - Abre transação
     * - Atualiza dados na tabela obras
     * - Atualiza relacionamento autor (REMOVE e RE-INSERT)
     * - Atualiza/inserir dados na tabela específica conforme tipo
     * - Commit / rollback em caso de erro
     */
    public void atualizar(Obra obra) throws SQLException {
        if (obra.getId() == null) {
            throw new SQLException("ID nulo para atualizar.");
        }

        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            // Obter/garantir IDs de autor e editora (podem ter sido alterados)
            Integer idAutor = inserirOuObterAutor(conn, obra.getAutor());
            Integer idEditora = inserirOuObterEditora(conn, obra.getEditora());

            // Atualiza tabela obras com os campos principais
            final String sqlObra =
                    "UPDATE obras SET chamada=?, chamada_local=?, titulo=?, edicao=?, " +
                            "ano_publicacao=?, id_editora=? WHERE id_obra=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlObra)) {
                ps.setString(1, obra.getChamada());
                ps.setString(2, obra.getChamada());
                ps.setString(3, obra.getTitulo());
                ps.setString(4, obra.getEdicao());
                ps.setInt(5, Integer.parseInt(obra.getAno_Publicacao()));
                ps.setInt(6, idEditora);
                ps.setInt(7, obra.getId());
                ps.executeUpdate();
            }

            // Atualiza relacionamento autor: remove antigos e insere o atual
            final String sqlDeleteAutor = "DELETE FROM obras_autores WHERE id_obra=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteAutor)) {
                ps.setInt(1, obra.getId());
                ps.executeUpdate();
            }

            final String sqlInsertAutor = "INSERT INTO obras_autores (id_obra, id_autor) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsertAutor)) {
                ps.setInt(1, obra.getId());
                ps.setInt(2, idAutor);
                ps.executeUpdate();
            }

            // Atualiza a tabela específica dependendo do tipo (se já existir atualiza, se não insere)
            String tipoObra = obra.getTipo_Obra();

            if ("Livro".equals(tipoObra)) {
                if (existeNaTabela(conn, "livros", obra.getId())) {
                    final String sql = "UPDATE livros SET isbn=? WHERE id_obra=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, obra.getIsbn());
                        ps.setInt(2, obra.getId());
                        ps.executeUpdate();
                    }
                } else {
                    final String sql = "INSERT INTO livros (id_obra, isbn) VALUES (?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, obra.getId());
                        ps.setString(2, obra.getIsbn());
                        ps.executeUpdate();
                    }
                }
            } else if ("Revista".equals(tipoObra)) {
                if (existeNaTabela(conn, "revistas", obra.getId())) {
                    // Atualiza revista existente
                    final String sql = "UPDATE revistas SET issn=?, volume=?, numero=? WHERE id_obra=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, obra.getIsbn() != null ? obra.getIsbn() : "");
                        if (obra.getVolume() != null && !obra.getVolume().isEmpty()) {
                            ps.setString(2, obra.getVolume());
                        } else {
                            ps.setNull(2, java.sql.Types.VARCHAR);
                        }
                        if (obra.getEdicao() != null && !obra.getEdicao().isEmpty()) {
                            ps.setString(3, obra.getEdicao());
                        } else {
                            ps.setNull(3, java.sql.Types.VARCHAR);
                        }
                        ps.setInt(4, obra.getId());
                        ps.executeUpdate();
                    }
                } else {
                    // Insere nova revista se não existia
                    final String sqlInsert = "INSERT INTO revistas (id_obra, issn, volume, numero) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                        ps.setInt(1, obra.getId());
                        ps.setString(2, obra.getIsbn() != null ? obra.getIsbn() : "");
                        if (obra.getVolume() != null && !obra.getVolume().isEmpty()) {
                            ps.setString(3, obra.getVolume());
                        } else {
                            ps.setNull(3, java.sql.Types.VARCHAR);
                        }
                        if (obra.getEdicao() != null && !obra.getEdicao().isEmpty()) {
                            ps.setString(4, obra.getEdicao());
                        } else {
                            ps.setNull(4, java.sql.Types.VARCHAR);
                        }
                        ps.executeUpdate();
                    }
                }
            } else if ("Jornal".equals(tipoObra)) {
                if (existeNaTabela(conn, "jornais", obra.getId())) {
                    final String sql = "UPDATE jornais SET issn=?, numero_edicao=? WHERE id_obra=?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, obra.getIsbn());
                        ps.setString(2, obra.getEdicao()); // numero_edicao usa campo edicao
                        ps.setInt(3, obra.getId());
                        ps.executeUpdate();
                    }
                }
                // Observação: se jornal não existia e precisa ser inserido, podemos adicionar lógica semelhante a revista/livro.
            }

            conn.commit();
        } catch (SQLException e) {
            // rollback em caso de falha
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // repropaga para camada superior
        } finally {
            // restaura autoCommit e fecha conexão
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Exclui uma obra pelo id.
     *
     * Passos:
     * - Excluir registros nas tabelas específicas (livros, revistas, jornais, etc).
     * - Excluir relacionamento em obras_autores.
     * - Excluir registro em obras.
     * - Tudo dentro de uma transação para garantir consistência.
     */
    public void excluir(int id) throws SQLException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            // Remove nas tabelas específicas (se existirem registros)
            excluirDaTabela(conn, "livros", id);
            excluirDaTabela(conn, "livros_online", id);
            excluirDaTabela(conn, "revistas", id);
            excluirDaTabela(conn, "jornais", id);

            // Remove relacionamento autor <-> obra
            final String sqlDeleteObraAutor = "DELETE FROM obras_autores WHERE id_obra=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteObraAutor)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // Remove obra
            final String sqlDeleteObra = "DELETE FROM obras WHERE id_obra=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteObra)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            // rollback em caso de erro
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            // restauração e fechamento da conexão
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Lista todas as obras com informações agregadas (autor, editora, tipo e campos específicos).
     *
     * A query utiliza LEFT JOINs para trazer dados das tabelas relacionadas e um CASE para determinar o tipo.
     */
    public List<Obra> listar() throws SQLException {
        final String sql =
                "SELECT " +
                        "    o.id_obra, o.chamada, o.titulo, o.edicao, o.ano_publicacao, " +
                        "    a.nome as autor_nome, " +
                        "    e.nome as editora_nome, " +
                        "    l.isbn, " +
                        "    r.issn as revista_issn, r.volume, r.numero as revista_numero, " +
                        "    j.issn as jornal_issn, j.numero_edicao, " +
                        "    lo.id_obra as livro_online_id, " +
                        "    CASE " +
                        "        WHEN l.id_obra IS NOT NULL THEN 'Livro' " +
                        "        WHEN lo.id_obra IS NOT NULL THEN 'Livro Online' " +
                        "        WHEN r.id_obra IS NOT NULL THEN 'Revista' " +
                        "        WHEN j.id_obra IS NOT NULL THEN 'Jornal' " +
                        "        ELSE 'Desconhecido' " +
                        "    END as tipo_obra " +
                        "FROM obras o " +
                        "LEFT JOIN obras_autores oa ON o.id_obra = oa.id_obra " +
                        "LEFT JOIN autores a ON oa.id_autor = a.id_autor " +
                        "LEFT JOIN editoras e ON o.id_editora = e.id_editora " +
                        "LEFT JOIN livros l ON o.id_obra = l.id_obra " +
                        "LEFT JOIN livros_online lo ON o.id_obra = lo.id_obra " +
                        "LEFT JOIN revistas r ON o.id_obra = r.id_obra " +
                        "LEFT JOIN jornais j ON o.id_obra = j.id_obra " +
                        "ORDER BY o.id_obra DESC";

        List<Obra> lista = new ArrayList<>();
        try (Connection conn = Db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapObra(rs)); // converte ResultSet em objeto modelo
            }
        }
        return lista;
    }

    /**
     * Mapeia um ResultSet para o objeto Obra.
     * Centraliza a lógica de leitura dos campos vindos da query complexa.
     */
    private Obra mapObra(ResultSet rs) throws SQLException {
        Obra obra = new Obra();
        obra.setId(rs.getInt("id_obra"));
        obra.setChamada(rs.getString("chamada"));
        obra.setTitulo(rs.getString("titulo"));
        obra.setAno_Publicacao(String.valueOf(rs.getInt("ano_publicacao")));
        obra.setAutor(rs.getString("autor_nome"));
        obra.setEditora(rs.getString("editora_nome"));
        obra.setTipo_Obra(rs.getString("tipo_obra"));

        // Preenche campos específicos com base no tipo detectado pelo SELECT CASE
        String tipo = rs.getString("tipo_obra");

        if ("Livro".equals(tipo)) {
            obra.setIsbn(rs.getString("isbn"));
            obra.setEdicao(rs.getString("edicao")); // edição do livro
        } else if ("Revista".equals(tipo)) {
            obra.setIsbn(rs.getString("revista_issn"));
            obra.setVolume(rs.getString("volume"));
            obra.setEdicao(rs.getString("revista_numero")); // numero -> edicao
        } else if ("Jornal".equals(tipo)) {
            obra.setIsbn(rs.getString("jornal_issn"));
            obra.setEdicao(rs.getString("numero_edicao")); // numero_edicao -> edicao
        } else if ("Livro Online".equals(tipo)) {
            obra.setEdicao(rs.getString("edicao")); // pode usar campo edicao se preenchido
        }

        return obra;
    }

    /**
     * Verifica se existe registro na tabela informada para a obra (usado para decidir UPDATE ou INSERT).
     * Utiliza um COUNT simples.
     */
    private boolean existeNaTabela(Connection conn, String tabela, int idObra) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM " + tabela + " WHERE id_obra=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idObra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Exclui registro de uma tabela específica para a obra.
     * Usado durante o processo de exclusão para limpar dados dependentes.
     */
    private void excluirDaTabela(Connection conn, String tabela, int idObra) throws SQLException {
        final String sql = "DELETE FROM " + tabela + " WHERE id_obra=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idObra);
            ps.executeUpdate();
        }
    }

    /**
     * Insere ou obtém ID do autor a partir do nome.
     *
     * Estratégia:
     * - Primeiro tenta localizar autor pelo nome (SELECT).
     * - Se existir, retorna o id.
     * - Se não existir, insere e retorna o id gerado.
     *
     * Isso evita duplicar autores com o mesmo nome.
     */
    private Integer inserirOuObterAutor(Connection conn, String nomeAutor) throws SQLException {
        final String sqlCheck = "SELECT id_autor FROM autores WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, nomeAutor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_autor");
                }
            }
        }

        final String sqlInsert = "INSERT INTO autores (nome) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nomeAutor);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // id gerado
                }
            }
        }
        throw new SQLException("Falha ao inserir autor.");
    }

    /**
     * Insere ou obtém ID da editora (mesma lógica de autores).
     */
    private Integer inserirOuObterEditora(Connection conn, String nomeEditora) throws SQLException {
        final String sqlCheck = "SELECT id_editora FROM editoras WHERE nome = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, nomeEditora);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_editora");
                }
            }
        }

        final String sqlInsert = "INSERT INTO editoras (nome) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nomeEditora);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Falha ao inserir editora.");
    }
}
