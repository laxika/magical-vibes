package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.UUID;

/**
 * Each player may discard their hand and then draw the evaluated number of cards.
 *
 * <p>The player choices are carried in active-player-first order so all choices are made before
 * any accepted player's hand changes.
 */
public record EachPlayerMayDiscardHandThenDrawEffect(
        DynamicAmount cardsToDraw,
        UUID sourceControllerId,
        List<UUID> remainingPlayerIds,
        List<UUID> acceptedPlayerIds,
        AcceptedPlayersAwareEffect acceptedPlayersFollowUp
) implements CardEffect {

    public EachPlayerMayDiscardHandThenDrawEffect(int cardsToDraw) {
        this(new Fixed(cardsToDraw), null, List.of(), List.of(), null);
    }

    public EachPlayerMayDiscardHandThenDrawEffect(DynamicAmount cardsToDraw) {
        this(cardsToDraw, null, List.of(), List.of(), null);
    }

    public EachPlayerMayDiscardHandThenDrawEffect(int cardsToDraw,
                                                   AcceptedPlayersAwareEffect acceptedPlayersFollowUp) {
        this(new Fixed(cardsToDraw), null, List.of(), List.of(), acceptedPlayersFollowUp);
    }

    public EachPlayerMayDiscardHandThenDrawEffect(DynamicAmount cardsToDraw,
                                                   AcceptedPlayersAwareEffect acceptedPlayersFollowUp) {
        this(cardsToDraw, null, List.of(), List.of(), acceptedPlayersFollowUp);
    }

    public EachPlayerMayDiscardHandThenDrawEffect {
        if (cardsToDraw == null) {
            throw new IllegalArgumentException("EachPlayerMayDiscardHandThenDrawEffect requires a draw amount");
        }
        remainingPlayerIds = List.copyOf(remainingPlayerIds);
        acceptedPlayerIds = List.copyOf(acceptedPlayerIds);
    }
}
