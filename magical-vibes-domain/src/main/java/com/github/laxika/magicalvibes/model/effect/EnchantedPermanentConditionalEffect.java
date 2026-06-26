package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper for aura static effects: checks whether the enchanted permanent matches
 * a predicate. If it does, the {@code ifMatch} effect is active; otherwise, the {@code ifNotMatch}
 * effect is active.
 */
public record EnchantedPermanentConditionalEffect(
        PermanentPredicate filter,
        CardEffect ifMatch,
        CardEffect ifNotMatch
) implements CardEffect {
}

