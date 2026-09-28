package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Has the controller choose one matching permanent to exile, then exiles every permanent matching
 * a second predicate.
 */
public record ExilePermanentThenExileMatchingPermanentsEffect(
        PermanentPredicate choiceFilter,
        PermanentPredicate matchingFilter,
        String choiceLabel
) implements CardEffect {
}
