package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Delayed end-step effect that makes each player discard their hand, then returns that player's
 * cards remembered by {@link ExileEachPlayerHandFaceDownAndReturnAtNextEndStepEffect}.
 */
public record DiscardEachPlayerHandAndReturnExiledCardsEffect(
        List<PlayerCards> players) implements CardEffect {

    public DiscardEachPlayerHandAndReturnExiledCardsEffect {
        players = players == null ? List.of() : List.copyOf(players);
    }

    /** The cards a player had exiled from their hand. */
    public record PlayerCards(UUID playerId, List<UUID> cardIds) {

        public PlayerCards {
            cardIds = cardIds == null ? List.of() : List.copyOf(cardIds);
        }
    }
}
