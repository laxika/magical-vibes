package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static attacking restriction: this creature can't attack unless its controller
 * controls at least one permanent matching the given predicate.
 * <p>
 * Example: Desperate Castaways — "can't attack unless you control an artifact."
 */
public record CantAttackUnlessControllerControlsMatchingPermanentEffect(
        PermanentPredicate controllerPermanentPredicate,
        String requirementDescription
) implements CardEffect {
}
