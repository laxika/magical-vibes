package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Static replacement marker for an effect that returns a dying creature to its owner's hand.
 */
public interface DyingCreatureReturnToHandReplacementEffect extends CardEffect {

    /**
     * Returns whether this replacement effect applies to the source and the creature that would
     * die.
     */
    default boolean appliesTo(Permanent source, Permanent dyingCreature, boolean dyingCreatureIsEnchanted,
                              UUID sourceControllerId, UUID dyingCreatureControllerId) {
        return dyingCreatureIsEnchanted
                && sourceControllerId != null
                && sourceControllerId.equals(dyingCreatureControllerId);
    }

    /**
     * Returns whether the returned card is revealed in its owner's hand until that player's next
     * turn.
     */
    default boolean revealsReturnedCardUntilOwnerNextTurn() {
        return false;
    }

    /**
     * Returns whether the returned card can't be played from its owner's hand until that player's
     * next turn.
     */
    default boolean preventsPlayingReturnedCardUntilOwnerNextTurn() {
        return false;
    }
}
