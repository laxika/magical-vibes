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
 * <p>{@code requireFirstTargetControlledByController} is {@code true} for cards whose wording pins
 * the first target to the ability's controller ("target land you control and target land an opponent
 * controls"). Cards that just say "two target creatures" (Switcheroo) pass {@code false}: either
 * target may be controlled by anyone, and the exchange happens whenever the two permanents have
 * different controllers.
 *
 * <p>Used by Puca's Mischief's upkeep trigger (target selection is mandatory at trigger time; the
 * "you may" is honoured at resolution by wrapping this effect in a {@link MayEffect}, see Axis of
 * Mortality) and by Political Trickery's and Switcheroo's two-target spells. At resolution the
 * exchange only happens if both targets are still legal, and only if the two permanents have
 * different controllers (CR 701.12a, CR 701.12b).
 */
public record ExchangeControlOfTargetPermanentsEffect(
        PermanentPredicate targetPredicate,
        boolean requireOpponentManaValueNotGreater,
        boolean requireFirstTargetControlledByController) implements CardEffect {

    public ExchangeControlOfTargetPermanentsEffect(
            PermanentPredicate targetPredicate, boolean requireOpponentManaValueNotGreater) {
        this(targetPredicate, requireOpponentManaValueNotGreater, true);
    }
}
