package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller of the targeted permanent chooses a matching permanent to sacrifice, then the
 * follow-up effect is inserted into the resolving spell or ability.
 */
public record TargetPermanentControllerSacrificeThenEffect(
        PermanentPredicate filter,
        CardEffect thenEffect,
        String permanentDescription
) implements CardEffect {
}
