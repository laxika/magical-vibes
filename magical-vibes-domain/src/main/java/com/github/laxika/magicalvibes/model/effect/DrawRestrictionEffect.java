package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Capability for static effects that can make a player's next draw impossible based on how many
 * cards that player has actually drawn this turn.
 */
public interface DrawRestrictionEffect extends CardEffect {

    default boolean appliesTo(UUID sourceControllerId, UUID drawingPlayerId) {
        return true;
    }

    boolean preventsDraw(int cardsDrawnThisTurn);
}
