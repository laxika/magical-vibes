package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that prevents a damage event to its own permanent when the event is at most the
 * configured amount.
 */
public record PreventDamageUpToAmountToSelfEffect(int amount) implements SelfDamagePreventionEffect {

    @Override
    public int preventedDamage(int damage) {
        return damage > 0 && damage <= amount ? damage : 0;
    }
}
