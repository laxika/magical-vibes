package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: "If a source would deal damage to a matching creature you control, prevent N of
 * that damage."
 */
public record PreventFixedDamagePerSourceToCreaturesYouControlEffect(
        PermanentPredicate filter,
        int amount
) implements FilteredCreaturesDamagePreventionEffect {

    public PreventFixedDamagePerSourceToCreaturesYouControlEffect(int amount) {
        this(null, amount);
    }

    public PreventFixedDamagePerSourceToCreaturesYouControlEffect {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
