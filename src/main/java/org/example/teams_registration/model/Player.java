package org.example.teams_registration.model;

import java.time.LocalDate;

public class Player {
    private String names;
    private String surnames;
    private String email;
    private String alias;
    private LocalDate birthdate;

    public Player(String names, String surnames, String email, String alias, LocalDate birthdate) {
        this.names = names;
        this.surnames = surnames;
        this.email = email;
        this.alias = alias;
        this.birthdate = birthdate;
    }

    public String getNames() {
        return names;
    }

    public String getSurnames() {
        return surnames;
    }

    public String getEmail() {
        return email;
    }

    public String getAlias() {
        return alias;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }
// Getters y setters
}

