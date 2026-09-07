package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice trigger effect: exile the top {@code count} cards of the target player's library and
 * let the ability controller play them until their next end step, using mana of any type for the
 * exiled spells.
 */
public record ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect(int count)
        implements CardEffect, SacrificedPermanentManaValueAwareEffect {

    public ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect() {
        this(0);
    }

    @Override
    public CardEffect boundToSacrificedPermanentManaValue(int manaValue) {
        return new ExileTargetPlayerTopCardsForSacrificedManaValueMayPlayUntilNextEndStepEffect(
                Math.max(0, manaValue));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
