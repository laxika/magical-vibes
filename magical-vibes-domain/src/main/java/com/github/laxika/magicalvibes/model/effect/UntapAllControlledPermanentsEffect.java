package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untaps all permanents controlled by the effect's controller that match the given filter.
 * If filter is null, untaps all permanents the controller controls.
 *
 * @param filter optional predicate to filter which permanents to untap
 */
public record UntapAllControlledPermanentsEffect(PermanentPredicate filter) implements CardEffect {
}
