package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Deals damage to any target equal to the number of permanents with the given subtype the controller
 * controls. When {@code gainLife} is true, the controller also gains life equal to the damage amount.
 * Used by Corrupt (Swamps, gainLife=true).
 */
public record DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffect(CardSubtype subtype, boolean gainLife) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
