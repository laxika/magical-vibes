package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Static conditional attack requirement (CR 508.1d, "attacks if some condition is met"): if a creature matching
 * {@code attackers} is declared as an attacker, each creature the declaring player controls that matches
 * {@code required} attacks if able. Both filters are judged with this permanent as their source, as attackers are
 * declared, so the requirement depends on the declaration itself rather than on the board. Like every requirement it
 * never makes a player pay an attack cost, and it gives way to a restriction (one of them can only attack alone, no more
 * creatures may attack). Read by {@code CombatAttackService} through the active static abilities, so a source that is
 * face down or lost its abilities imposes nothing.
 *
 * <p>Forms: {@code (CounterType)} "if a creature with a [kind] counter on it attacks, all creatures with [kind] counters
 * on them attack if able" (Magnetic Web); {@link #ifSourceAttacks()} "if this creature attacks, all creatures you control
 * attack if able" (Viashino Bey); {@link #ifAnotherYouControlAttacks()} "if a creature you control attacks, this
 * creature also attacks if able" (Ekundu Cyclops); {@link #opponentsAttackTogether()} "if a creature an opponent
 * controls attacks, all creatures that opponent controls attack if able" (War's Toll).
 *
 * @param attackers what a declared attacker must be for the requirement to apply
 * @param required  which of the declaring player's other creatures then attack if able
 */
public record CreaturesWithCounterAttackTogetherEffect(PermanentPredicate attackers, PermanentPredicate required)
        implements CardEffect {

    /** Magnetic Web: creatures with {@code counterType} counters attack together. */
    public CreaturesWithCounterAttackTogetherEffect(CounterType counterType) {
        this(new PermanentHasCountersPredicate(counterType), new PermanentHasCountersPredicate(counterType));
    }

    /** "If this creature attacks, all creatures you control attack if able." */
    public static CreaturesWithCounterAttackTogetherEffect ifSourceAttacks() {
        return new CreaturesWithCounterAttackTogetherEffect(new PermanentIsSourcePermanentPredicate(),
                new PermanentControlledBySourceControllerPredicate());
    }

    /** "If a creature you control attacks, this creature also attacks if able." */
    public static CreaturesWithCounterAttackTogetherEffect ifAnotherYouControlAttacks() {
        return new CreaturesWithCounterAttackTogetherEffect(new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PermanentControlledBySourceControllerPredicate())),
                new PermanentIsSourcePermanentPredicate());
    }

    /** "If a creature an opponent controls attacks, all creatures that opponent controls attack if able." */
    public static CreaturesWithCounterAttackTogetherEffect opponentsAttackTogether() {
        PermanentPredicate opponents = new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());
        return new CreaturesWithCounterAttackTogetherEffect(opponents, opponents);
    }
}
