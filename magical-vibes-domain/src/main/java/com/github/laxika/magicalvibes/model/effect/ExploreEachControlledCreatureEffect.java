package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Makes each creature controlled by the resolving player that matches {@code predicate} explore.
 * The matching permanents are snapshotted when this effect resolves, and their explores are then
 * inserted as individual effects so each one keeps the normal explore choices and triggers.
 */
public record ExploreEachControlledCreatureEffect(PermanentPredicate predicate) implements CardEffect {
}
