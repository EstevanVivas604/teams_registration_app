package org.example.teams_registration.controller;

import com.google.gson.JsonObject;
import org.example.teams_registration.model.Player;
import org.example.teams_registration.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Optional;

@RestController
@RequestMapping("/players")
public class PlayersController {
    private PlayerRepository repository = new PlayerRepository();

    @GetMapping
    public ResponseEntity<JsonObject> getPlayers(@RequestParam(value = "email", required = false) String email,
                                                 @RequestParam(value = "alias", required = false) String alias)
            throws SQLException, MissingServletRequestParameterException {

        if (email != null) {
            return findByEmail(email);
        }

        if (alias != null) {
            return findByAlias(alias);
        }

        throw new MissingServletRequestParameterException("email or alias", "String");
    }

    private ResponseEntity<JsonObject> findByEmail(String email) throws SQLException {
        if (email.isEmpty()) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Email cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<Player> optionalPlayer = repository.findByEmail(email);
        JsonObject response = new JsonObject();
        response.addProperty("email", email);
        response.addProperty("found", optionalPlayer.isPresent());

        return optionalPlayer.isPresent()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    private ResponseEntity<JsonObject> findByAlias(String alias) throws SQLException {
        if (alias.isEmpty()) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("error", "Alias cannot be empty");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Optional<Player> optionalPlayer = repository.findByAlias(alias);
        JsonObject response = new JsonObject();
        response.addProperty("alias", alias);
        response.addProperty("found", optionalPlayer.isPresent());
        return optionalPlayer.isPresent()
                ? ResponseEntity.ok(response)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
