package com.github.laxika.magicalvibes.model.effect;

/** Floating, turn-scoped attack tax paid in life for each attacking creature. */
public record GlobalAttackLifeTaxEffect(int lifeCostPerCreature, boolean protectsPlaneswalkers)
        implements GlobalAttackLifeCostEffect {

    public GlobalAttackLifeTaxEffect(int lifeCostPerCreature) {
        this(lifeCostPerCreature, true);
    }
}
