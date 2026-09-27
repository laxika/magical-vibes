package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Each player may discard their hand and then draw a fixed number of cards.
 *
 * <p>The player choices are carried in active-player-first order so all choices are made before
 * any accepted player's hand changes.
 */
public record EachPlayerMayDiscardHandThenDrawEffect(
        int cardsToDraw,
        UUID sourceControllerId,
        List<UUID> remainingPlayerIds,
        List<UUID> acceptedPlayerIds,
        AcceptedPlayersAwareEffect acceptedPlayersFollowUp
) implements CardEffect {

    public EachPlayerMayDiscardHandThenDrawEffect(int cardsToDraw) {
        this(cardsToDraw, null, List.of(), List.of(), null);
    }

    public EachPlayerMayDiscardHandThenDrawEffect(int cardsToDraw,
                                                   AcceptedPlayersAwareEffect acceptedPlayersFollowUp) {
        this(cardsToDraw, null, List.of(), List.of(), acceptedPlayersFollowUp);
    }

    public EachPlayerMayDiscardHandThenDrawEffect {
        remainingPlayerIds = List.copyOf(remainingPlayerIds);
        acceptedPlayerIds = List.copyOf(acceptedPlayerIds);
    }
}
