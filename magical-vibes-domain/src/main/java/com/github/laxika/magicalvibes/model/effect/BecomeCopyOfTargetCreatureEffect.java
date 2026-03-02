package com.github.laxika.magicalvibes.model.effect;

/**
 * Causes the source permanent to become a copy of the target creature,
 * except it retains the triggered ability that granted this copy effect.
 * Used by Cryptoplasm and similar shapeshifters.
 */
public record BecomeCopyOfTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
