package com.github.laxika.magicalvibes.model.effect;

/**
 * Floating, turn-scoped global attack tax created by an effect such as War Tax.
 */
public record GlobalAttackTaxEffect(int attackCostPerCreature, boolean protectsPlaneswalkers)
        implements GlobalAttackCostEffect {

    public GlobalAttackTaxEffect(int attackCostPerCreature) {
        this(attackCostPerCreature, true);
    }
}
