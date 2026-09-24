package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission to spend mana as though it were mana of any color to cast spells the
 * controller does not own.
 */
public record SpendAnyManaTypeToCastSpellsYouDontOwnEffect() implements AnyManaTypeCastEffect {

    @Override
    public boolean appliesToNonOwnedSpellsOnly() {
        return true;
    }
}
