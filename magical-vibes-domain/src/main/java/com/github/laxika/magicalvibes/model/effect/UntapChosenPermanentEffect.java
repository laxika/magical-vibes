package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Untap target [permanent]" resolved as a controller choice at resolution rather than a cast-time
 * target. Used by triggered abilities on slots with no targeting pipeline (e.g. combat-damage
 * triggers such as Initiate's Companion's "untap target creature or land"): the effect is pushed as
 * a non-targeting stack entry and, at resolution, the controller chooses one permanent matching
 * {@code predicate} across every battlefield to untap.
 *
 * @param predicate filter restricting the choosable permanents (e.g. "creature or land")
 */
public record UntapChosenPermanentEffect(PermanentPredicate predicate) implements CardEffect {
}
