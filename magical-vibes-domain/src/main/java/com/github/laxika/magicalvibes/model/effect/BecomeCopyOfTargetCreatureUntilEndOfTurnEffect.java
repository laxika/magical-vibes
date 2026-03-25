package com.github.laxika.magicalvibes.model.effect;

/**
 * Causes the source permanent to become a copy of the target creature until end of turn.
 * At the cleanup step, the permanent reverts to its original card.
 * Used by Tilonalli's Skinshifter and similar shapeshifters.
 */
public record BecomeCopyOfTargetCreatureUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
