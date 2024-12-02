package org.example.teams_registration.repository;

import org.example.teams_registration.model.Player;
import org.example.teams_registration.model.Team;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TeamRepository {
    private Connection connection = DatabaseConnection.getConnection();

    private PlayerRepository playerRepository = new PlayerRepository();

    public List<Team> findAll() throws SQLException {
        List<Team> teams = new ArrayList<>();
        String query = """
                SELECT team_id, team_name, captain_id
                FROM teams
                """;

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                teams.add(parseResultSet(resultSet));
            }
        }

        return Collections.unmodifiableList(teams);
    }

    public Optional<Team> findById(long id) throws SQLException {
        String query = "SELECT team_id, team_name, captain_id FROM teams WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, id);

            return executeQuery(preparedStatement);
        }
    }

    public Optional<Team> findByName(String teamName) throws SQLException {
        String query = "SELECT team_id, team_name, captain_id FROM teams WHERE team_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, teamName.toLowerCase().trim());

            return executeQuery(preparedStatement);
        }
    }

    private Optional<Team> executeQuery(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return Optional.of(parseResultSet(resultSet));
            }
        }

        return Optional.empty();
    }

    private Team parseResultSet(ResultSet resultSet) throws SQLException {
        String teamName = resultSet.getString("team_name");
        List<Player> players = playerRepository.findByTeam(resultSet.getLong("team_id"));
        Player captain = playerRepository.findById(resultSet.getLong("captain_id"))
                .orElseThrow(() -> new SQLException("Captain not found"));

        return new Team(teamName, players, captain);
    }

    public void save(Team team) throws SQLException {
        validateNameUniqueness(team.getName());
        long teamId = insertTeam(team.getName());

        savePlayers(team.getPlayers(), teamId);

        long captainId = playerRepository.getPlayerId(team.getCaptain().getAlias())
                .orElseThrow(() -> new SQLException("Captain not found after player insertion."));

        assignCaptainToTeam(teamId, captainId);
    }

    private long insertTeam(String teamName) throws SQLException {
        String query = "INSERT INTO teams (team_name) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, teamName.trim());
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }

        throw new SQLException("Failed to insert team.");
    }

    private void savePlayers(List<Player> players, long teamId) throws SQLException {
        for (Player player : players) {
            playerRepository.save(player, teamId);
        }
    }

    private void assignCaptainToTeam(long teamId, long captainId) throws SQLException {
        String query = "UPDATE teams SET captain_id = ? WHERE team_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setLong(1, captainId);
            preparedStatement.setLong(2, teamId);

            if (preparedStatement.executeUpdate() == 0) {
                throw new SQLException("Failed to assign captain to team.");
            }
        }
    }

    private void validateNameUniqueness(String name) throws SQLException {
        if (findByName(name).isPresent()) {
            throw new IllegalArgumentException("Name '" + name + "' is already in use.");
        }
    }
}
