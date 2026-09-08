package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static ability that makes opponent spells targeting a permanent matching the predicate
 * and controlled by this effect's controller cost additional life to cast.
 */
public record IncreaseOpponentLifeCostForTargetingControlledPermanentEffect(
        PermanentPredicate predicate, int amount) implements CardEffect {
}
