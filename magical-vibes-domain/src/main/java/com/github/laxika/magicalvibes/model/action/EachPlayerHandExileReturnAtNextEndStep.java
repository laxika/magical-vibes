package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/**
 * Delayed triggered ability for an effect that exiled every player's hand face down and remembers
 * which cards each player exiled until the next end step.
 */
public record EachPlayerHandExileReturnAtNextEndStep(
        Card sourceCard,
        UUID controllerId,
        List<PlayerCards> players) implements DelayedAction {

    public EachPlayerHandExileReturnAtNextEndStep {
        players = players == null ? List.of() : List.copyOf(players);
    }

    public record PlayerCards(UUID playerId, List<UUID> cardIds) {

        public PlayerCards {
            cardIds = cardIds == null ? List.of() : List.copyOf(cardIds);
        }
    }
}
