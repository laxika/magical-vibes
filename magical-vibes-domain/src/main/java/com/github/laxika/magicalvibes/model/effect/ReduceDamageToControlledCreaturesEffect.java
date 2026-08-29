package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that reduces damage dealt to creatures controlled by the source's
 * controller.
 */
public record ReduceDamageToControlledCreaturesEffect(int amount)
        implements ControlledCreaturesDamageReductionEffect {

    public ReduceDamageToControlledCreaturesEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
