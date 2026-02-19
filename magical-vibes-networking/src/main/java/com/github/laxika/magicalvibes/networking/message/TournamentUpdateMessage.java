package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;

public record TournamentUpdateMessage(
        MessageType type,
        List<TournamentRound> rounds,
        int currentRound,
        String roundName
) {
    public TournamentUpdateMessage(List<TournamentRound> rounds, int currentRound, String roundName) {
        this(MessageType.TOURNAMENT_UPDATE, rounds, currentRound, roundName);
    }

    public record TournamentRound(String roundName, List<TournamentPairing> pairings) {}

    public record TournamentPairing(String player1Name, String player2Name, String winnerName) {}
}
