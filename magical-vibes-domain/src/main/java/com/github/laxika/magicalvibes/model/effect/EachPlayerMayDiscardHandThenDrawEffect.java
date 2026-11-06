package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.UUID;

/**
 * Each player may discard their hand and then draw the evaluated number of cards.
 *
 * <p>The player choices are carried in active-player-first order so all choices are made before
 * any accepted player's hand changes. {@code acceptedPlayersFollowUp} ("then ... each opponent who discarded
 * their hand this way") resolves after the draws and reads the players who said yes from the stack entry
 * ({@code StackEntry.getAcceptedPlayerIds()}).
 */
public record EachPlayerMayDiscardHandThenDrawEffect(
        DynamicAmount cardsToDraw,
        UUID sourceControllerId,
        List<UUID> remainingPlayerIds,
        List<UUID> acceptedPlayerIds,
        CardEffect acceptedPlayersFollowUp
) implements CardEffect {

    public EachPlayerMayDiscardHandThenDrawEffect(int cardsToDraw) {
        this(new Fixed(cardsToDraw), null, List.of(), List.of(), null);
    }

    public EachPlayerMayDiscardHandThenDrawEffect(DynamicAmount cardsToDraw) {
        this(cardsToDraw, null, List.of(), List.of(), null);
    }

    public EachPlayerMayDiscardHandThenDrawEffect(int cardsToDraw,
                                                   CardEffect acceptedPlayersFollowUp) {
        this(new Fixed(cardsToDraw), null, List.of(), List.of(), acceptedPlayersFollowUp);
    }

    public EachPlayerMayDiscardHandThenDrawEffect(DynamicAmount cardsToDraw,
                                                   CardEffect acceptedPlayersFollowUp) {
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
