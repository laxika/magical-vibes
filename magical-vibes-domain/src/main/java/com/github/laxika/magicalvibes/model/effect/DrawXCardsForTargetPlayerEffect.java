package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player draws X cards, where X comes from the stack entry's xValue.
 * The target player is stored in the stack entry's targetPermanentId field.
 */
public record DrawXCardsForTargetPlayerEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
