package com.github.laxika.magicalvibes.model.effect;

/**
 * Each other creature becomes a copy of the target creature until end of turn. The target itself
 * is unaffected; every other creature on the battlefield (regardless of controller) takes on the
 * target's copiable values until the cleanup step. Used by Mirrorweave.
 */
public record EachOtherCreatureBecomesCopyOfTargetCreatureUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
