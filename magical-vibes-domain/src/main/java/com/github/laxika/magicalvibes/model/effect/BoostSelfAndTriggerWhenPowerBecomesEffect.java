package com.github.laxika.magicalvibes.model.effect;

/**
 * Boosts the source permanent and queues a linked effect when that boost changes its effective
 * power to the exact threshold.
 *
 * <p>The linked effect is intentionally not included in this effect's targeting descriptor. Its
 * targets are chosen only when the linked triggered ability is put on the stack.</p>
 */
public record BoostSelfAndTriggerWhenPowerBecomesEffect(
        BoostEffect boost,
        int powerThreshold,
        CardEffect triggeredEffect) implements CardEffect {

    public BoostSelfAndTriggerWhenPowerBecomesEffect(int powerBoost, int toughnessBoost,
                                                     int powerThreshold, int damage) {
        this(BoostEffect.self(powerBoost, toughnessBoost), powerThreshold,
                DealDamageEffect.toAnyTarget(damage));
    }
}
