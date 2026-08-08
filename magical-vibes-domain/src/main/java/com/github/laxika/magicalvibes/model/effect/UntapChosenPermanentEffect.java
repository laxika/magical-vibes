package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Untap target [permanent]" resolved as a controller choice at resolution rather than a cast-time
 * target. Used by triggered abilities on slots with no targeting pipeline (e.g. combat-damage
 * triggers such as Initiate's Companion's "untap target creature or land"): the effect is pushed as
 * a non-targeting stack entry and, at resolution, the controller chooses one permanent matching
 * {@code predicate} across every battlefield to untap.
 *
 * <p>With {@code targetPlayerChooses} the stack entry's target player picks instead of the
 * controller — used by {@code EACH_UPKEEP_TRIGGERED} abilities worded "that player untaps a land
 * they control" (Hokori, Dust Drinker). Restrict the candidates to that player's permanents through
 * {@code predicate} (e.g. a {@code PermanentControlledByActivePlayerPredicate} conjunct).
 *
 * @param predicate filter restricting the choosable permanents (e.g. "creature or land")
 * @param targetPlayerChooses whether the entry's target player, rather than its controller, chooses
 */
public record UntapChosenPermanentEffect(PermanentPredicate predicate, boolean targetPlayerChooses)
        implements CardEffect {

    public UntapChosenPermanentEffect(PermanentPredicate predicate) {
        this(predicate, false);
    }
}
