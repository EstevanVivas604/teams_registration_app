package org.example.teams_registration.model;

import java.util.List;

public class Team {
    private String name;
    private List<Player> players;
    private Player captain;

    public Team(String name, List<Player> players, Player captain) {
        this.name = name;
        this.players = players;
        this.captain = captain;
    }

    public Player getCaptain() {
        return captain;
    }

    public String getName() {
        return name;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
