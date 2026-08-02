package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Redirects the next amount of damage that would be dealt to the targeted creature this turn to
 * the ability's controller instead. The target is protected from any damage source.
 */
public record RedirectNextDamageToTargetCreatureToControllerEffect(DynamicAmount amount) implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
