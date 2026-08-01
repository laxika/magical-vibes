package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroy all other permanents with the same name as the permanent that triggered this ability.
 * They can't be regenerated. Reads the entering permanent from
 * {@code StackEntry.triggeringPermanentId} (name from last-known info via
 * {@code triggeringCardId} if it has left). Used by Eye of Singularity.
 */
public record DestroyOtherPermanentsWithEnteringNameEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
