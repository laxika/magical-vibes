package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, if a source the ability's controller controls would deal
 * damage to an opponent, it deals the configured multiple of that damage instead. Damage to permanents
 * controlled by opponents is unaffected. Used by Goblin Goliath.
 */
public record MultiplyControllerDamageToOpponentsThisTurnEffect(int multiplier) implements CardEffect {

    public MultiplyControllerDamageToOpponentsThisTurnEffect {
        if (multiplier < 1) {
            throw new IllegalArgumentException("Damage multiplier must be positive");
        }
    }
}
