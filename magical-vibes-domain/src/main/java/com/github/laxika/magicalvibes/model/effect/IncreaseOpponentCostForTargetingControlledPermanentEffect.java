package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static ability that increases the cost of opponent spells and activated abilities
 * that target a permanent matching the predicate and controlled by this effect's controller.
 * E.g. Kopala, Warden of Waves: {@code PermanentHasSubtypePredicate(MERFOLK)}, amount = 2.
 */
public record IncreaseOpponentCostForTargetingControlledPermanentEffect(PermanentPredicate predicate, int amount) implements CardEffect {
}
