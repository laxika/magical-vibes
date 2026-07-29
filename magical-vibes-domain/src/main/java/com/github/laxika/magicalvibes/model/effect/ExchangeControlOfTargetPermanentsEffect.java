package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exchanges control of the two permanents stored in {@code StackEntry.targetIds}: the first target
 * (a permanent the ability's controller controls) and the second target (a permanent an opponent
 * controls).
 *
 * <p>{@code targetPredicate} is the shape both targets must still match at resolution (nonland for
 * Puca's Mischief, land for Political Trickery); {@code requireOpponentManaValueNotGreater} adds
 * Puca's Mischief's "with equal or lesser mana value" restriction on the second target.
 *
 * <p>Used by Puca's Mischief's upkeep trigger (target selection is mandatory at trigger time; the
 * "you may" is honoured at resolution by wrapping this effect in a {@link MayEffect}, see Axis of
 * Mortality) and by Political Trickery's two-target spell. At resolution the exchange only happens
 * if both targets are still legal (CR 701.10).
 */
public record ExchangeControlOfTargetPermanentsEffect(
        PermanentPredicate targetPredicate,
        boolean requireOpponentManaValueNotGreater) implements CardEffect {
}
