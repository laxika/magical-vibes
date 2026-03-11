package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets a target creature's base power and toughness to the specified values until end of turn.
 * Modifiers (counters, boosts, static effects) still apply on top of the new base values.
 * Used by Diminish and similar effects.
 */
public record SetBasePowerToughnessUntilEndOfTurnEffect(int power, int toughness) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
