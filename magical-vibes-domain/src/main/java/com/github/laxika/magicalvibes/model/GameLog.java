package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;

public final class GameLog {

    private GameLog() {}

    public static GameLogEntry text(String message) {
        return GameLogEntry.text(message);
    }

    public static GameLogEntry entersBattlefieldUnder(Card card, String controllerName) {
        return builder().card(card).text(" enters the battlefield under " + controllerName + "'s control.").build();
    }

    public static GameLogEntry entersBattlefieldTappedUnder(Card card, String controllerName) {
        return builder().card(card).text(" enters the battlefield tapped under " + controllerName + "'s control.").build();
    }

    public static GameLogEntry entersBattlefieldWithUnder(Card card, String withPhrase, String controllerName) {
        return builder().card(card).text(" enters the battlefield with " + withPhrase + " under " + controllerName + "'s control.").build();
    }

    public static GameLogEntry playerChoosesForCard(String playerName, String choice, Card card) {
        return builder().text(playerName + " chooses ").text(choice).text(" for ").card(card).text(".").build();
    }

    public static GameLogEntry playerPlays(String playerName, Card card) {
        return playerPlays(playerName, card, ".");
    }

    public static GameLogEntry playerPlays(String playerName, Card card, String suffix) {
        return builder().text(playerName + " plays ").card(card).text(suffix).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<GameLogSegment> segments = new ArrayList<>();

        public Builder text(String text) {
            if (text != null && !text.isEmpty()) {
                segments.add(GameLogSegment.text(text));
            }
            return this;
        }

        public Builder card(Card card) {
            if (card != null) {
                segments.add(GameLogSegment.card(card));
            }
            return this;
        }

        public GameLogEntry build() {
            return new GameLogEntry(List.copyOf(segments));
        }
    }
}
