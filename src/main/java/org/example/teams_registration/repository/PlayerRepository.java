package org.example.teams_registration.repository;

import org.example.teams_registration.model.Player;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlayerRepository {

    private Connection connection = DatabaseConnection.getConnection();

    public Optional<Player> findById(long playerId) throws SQLException {
        String query = """
                SELECT names, surnames, email, birthdate, alias
                FROM players
                WHERE player_id = ?
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, playerId);
            return executeQuery(preparedStatement);
        }
    }

    public Optional<Player> findByAlias(String alias) throws SQLException {
        String query = """
                SELECT names, surnames, email, birthdate, alias
                FROM players
                WHERE alias = ?
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, alias.toLowerCase().trim());
            return executeQuery(preparedStatement);
        }
    }

    public Optional<Player> findByEmail(String email) throws SQLException {
        String query = """
                SELECT names, surnames, email, birthdate, alias
                FROM players
                WHERE email = ?
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email.trim().toLowerCase());
            return executeQuery(preparedStatement);
        }
    }

    private Optional<Player> executeQuery(PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                Player found = parseResultSet(resultSet);
                return Optional.of(found);
            }
        }

        return Optional.empty();
    }

    public List<Player> findByTeam(long teamId) throws SQLException {
        List<Player> result = new ArrayList<>();
        String query = """
                SELECT names, surnames, email, birthdate, alias
                FROM players
                WHERE team_id = ?
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, teamId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(parseResultSet(resultSet));
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    private Player parseResultSet(ResultSet resultSet) throws SQLException {
        String names = resultSet.getString("names");
        String surnames = resultSet.getString("surnames");
        String email = resultSet.getString("email");
        String alias = resultSet.getString("alias");
        LocalDate birthdate = resultSet.getDate("birthdate").toLocalDate();

        return new Player(names, surnames, email, alias, birthdate);
    }

    public void save(Player player, long teamId) throws SQLException {
        validateEmailUniqueness(player.getEmail());
        validateAliasUniqueness(player.getAlias());

        String query = """        
                INSERT INTO players (names, surnames, email, birthdate, alias, team_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, player.getNames().trim());
            preparedStatement.setString(2, player.getSurnames().trim());
            preparedStatement.setString(3, player.getEmail().trim().toLowerCase());
            preparedStatement.setDate(4, Date.valueOf(player.getBirthdate()));
            preparedStatement.setString(5, player.getAlias().trim());
            preparedStatement.setLong(6, teamId);

            if (preparedStatement.executeUpdate() == 0) {
                throw new SQLException("Failed to insert player");
            }
        }
    }

    private void validateAliasUniqueness(String alias) throws SQLException {
        if (findByAlias(alias).isPresent()) {
            throw new IllegalArgumentException("Alias '" + alias + "' is already in use");
        }
    }

    private void validateEmailUniqueness(String email) throws SQLException {
        if (findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email '" + email + "' is already in use.");
        }
    }

    public Optional<Long> getPlayerId(String alias) throws SQLException {
        String query = """
                SELECT player_id
                FROM players
                WHERE alias = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, alias.trim().toLowerCase());

            try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()){
                    return Optional.of(resultSet.getLong("player_id"));
                }
            }
        }

        return Optional.empty();
    }
}
