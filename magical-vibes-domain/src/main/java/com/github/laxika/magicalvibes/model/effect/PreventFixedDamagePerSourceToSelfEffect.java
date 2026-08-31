package com.github.laxika.magicalvibes.model.effect;

/**
 * Static self-protection: "If a source would deal damage to this permanent, prevent N of that
 * damage." (absorb N)
 */
public record PreventFixedDamagePerSourceToSelfEffect(int amount) implements SelfDamagePreventionEffect {

    public PreventFixedDamagePerSourceToSelfEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    @Override
    public int preventedDamage(int damage) {
        return Math.min(amount, Math.max(0, damage));
    }
}
