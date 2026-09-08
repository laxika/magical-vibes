package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a source would deal damage to you or a creature you control, prevent N of that
 * damage."
 */
public record PreventFixedDamagePerSourceToControllerAndCreaturesEffect(int amount)
        implements ControllerAndCreaturesDamagePreventionEffect {

    public PreventFixedDamagePerSourceToControllerAndCreaturesEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
