package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents all damage to this permanent and makes an opponent of its controller gain control of it.
 */
public record PreventDamageToSelfAndOpponentGainsControlEffect()
        implements DamagePreventionControlChangeEffect {

    @Override
    public int preventedDamage(int damage) {
        return Math.max(0, damage);
    }
}
