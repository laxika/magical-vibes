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

    public static GameLogEntry playerTaps(String playerName, Card card) {
        return playerTaps(playerName, card, ".");
    }

    public static GameLogEntry playerTaps(String playerName, Card card, String suffix) {
        return builder().text(playerName + " taps ").card(card).text(suffix).build();
    }

    public static GameLogEntry isDestroyed(Card card) {
        return builder().card(card).text(" is destroyed.").build();
    }

    /**
     * Logs that a stack entry resolves. When {@code description} is the card name (spells) this is
     * "{card} resolves."; when it starts with the card name (e.g. "Iron Star's ability") the
     * remainder is kept after the card segment; when the card name appears mid-description
     * (e.g. "Copy of Lightning Bolt") the surrounding text is kept around the card segment.
     */
    public static GameLogEntry resolves(Card card, String description) {
        if (card == null) {
            return text(description + " resolves.");
        }
        String name = card.getName();
        if (description != null && description.startsWith(name)) {
            return builder().card(card).text(description.substring(name.length()) + " resolves.").build();
        }
        int nameIndex = description != null ? description.indexOf(name) : -1;
        if (nameIndex > 0) {
            return builder().text(description.substring(0, nameIndex)).card(card)
                    .text(description.substring(nameIndex + name.length()) + " resolves.").build();
        }
        return builder().card(card).text(" resolves.").build();
    }

    public static GameLogEntry playerDeclinesAbility(String playerName, Card card) {
        return builder().text(playerName + " declines ").card(card).text("'s ability.").build();
    }

    /** "{card}{suffix}" — for messages that start with a card name. */
    public static GameLogEntry cardThen(Card card, String suffix) {
        return builder().card(card).text(suffix).build();
    }

    /** "{prefix}{card}{suffix}" — for messages with a card name in the middle. */
    public static GameLogEntry textCardText(String prefix, Card card, String suffix) {
        return builder().text(prefix).card(card).text(suffix).build();
    }

    /** "{cardA}{mid}{cardB}{suffix}" — for messages naming two cards. */
    public static GameLogEntry cardTextCard(Card cardA, String mid, Card cardB, String suffix) {
        return builder().card(cardA).text(mid).card(cardB).text(suffix).build();
    }

    public static GameLogEntry abilityTriggers(Card card) {
        return cardThen(card, "'s ability triggers.");
    }

    public static GameLogEntry isSacrificed(Card card) {
        return cardThen(card, " is sacrificed.");
    }

    public static GameLogEntry isExiled(Card card) {
        return cardThen(card, " is exiled.");
    }

    public static GameLogEntry isIndestructible(Card card) {
        return cardThen(card, " is indestructible.");
    }

    public static GameLogEntry playerSacrifices(String playerName, Card card) {
        return textCardText(playerName + " sacrifices ", card, ".");
    }

    public static GameLogEntry playerDiscards(String playerName, Card card) {
        return textCardText(playerName + " discards ", card, ".");
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
