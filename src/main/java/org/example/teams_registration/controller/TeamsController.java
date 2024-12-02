package org.example.teams_registration.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.teams_registration.model.Player;
import org.example.teams_registration.model.Team;
import org.example.teams_registration.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teams")
public class TeamsController {
    private TeamRepository repository = new TeamRepository();

    @GetMapping
    public ResponseEntity<JsonObject> getTeam(@RequestParam("team") String teamName) throws SQLException {
        Optional<Team> optionalTeam = repository.findByName(teamName);

        if (optionalTeam.isEmpty()) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("team", teamName);
            errorResponse.addProperty("message", "no found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        JsonObject response = new JsonObject();
        response.addProperty("team", optionalTeam.get().getName());
        response.addProperty("message", "found");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<JsonObject> registerTeam(@RequestBody JsonObject teamData) throws SQLException {
        try {
            Team team = processTeamData(teamData);
            repository.save(team);
            JsonObject response = new JsonObject();
            response.addProperty("team", team.getName());
            response.addProperty("message", "Team registered successfully.");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            throw e;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("code", HttpStatus.BAD_REQUEST.value());
            errorResponse.addProperty("message", "Team registration failed. Invalid team data.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    private Team processTeamData(JsonObject teamData) {
        int NUMBER_OF_PLAYERS = 5;
        String teamName = teamData.get("name").getAsString().trim();
        if (teamName.isEmpty()) {
            throw new IllegalArgumentException();
        }

        JsonArray playersJson = teamData.getAsJsonArray("players");
        if (playersJson.size() != NUMBER_OF_PLAYERS) {
            throw new IllegalArgumentException();
        }

        List<Player> players = playersJson.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> {
                    String names = jsonObject.get("names").getAsString().trim();
                    String surnames = jsonObject.get("surnames").getAsString().trim();
                    String birthdateString = jsonObject.get("birthdate").getAsString().trim();
                    String email = jsonObject.get("email").getAsString().trim().toLowerCase();
                    String alias = jsonObject.get("alias").getAsString().trim();

                    if (names.isEmpty() || surnames.isEmpty() || birthdateString.isEmpty()
                            || email.isEmpty() || alias.isEmpty()) {
                        throw new IllegalArgumentException();
                    }

                    LocalDate birthdate = LocalDate.parse(birthdateString);

                    return new Player(names, surnames, email, alias,  birthdate);
                })
                .toList();

        int captainIdx = teamData.get("captainIdx").getAsInt();
        Player captain = players.get(captainIdx);

        return new Team(teamName, players, captain);
    }
}
