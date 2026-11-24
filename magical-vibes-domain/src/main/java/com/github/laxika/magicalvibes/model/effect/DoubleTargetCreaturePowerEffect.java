package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import java.util.List;

/**
 * "Double [creatures]' power" the evaluated number of times until end of turn: target creature
 * ({@code Subjects.target()}, the default), or a set found once as the effect resolves (CR 608.2h): "each creature you
 * control" ({@link #ofCreaturesYouControl}), "each other creature you control" ({@code otherCreaturesYouControl()}).
 * Each doubling gives a creature +X/+0 where X is its current power (a negative power doubled too), so repeated
 * doublings compound; Maro's Gone Nuts upgrades each doubling.
 *
 * @param times   how many times each creature's power is doubled
 * @param subject the creatures whose power is doubled
 */
public record DoubleTargetCreaturePowerEffect(DynamicAmount times, PermanentSubject subject)
        implements CardEffect, DoublingEffect {

    public DoubleTargetCreaturePowerEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
    }

    /** Doubles target creature's power {@code times} times. */
    public DoubleTargetCreaturePowerEffect(DynamicAmount times) {
        this(times, Subjects.target());
    }

    /** Doubles the power of each of {@code creatures} once. */
    public static DoubleTargetCreaturePowerEffect of(PermanentSubject creatures) {
        return new DoubleTargetCreaturePowerEffect(new Fixed(1), creatures);
    }

    /** "Double the power of each [filter] creature you control"; {@code null} for every creature you control. */
    public static DoubleTargetCreaturePowerEffect ofCreaturesYouControl(PermanentPredicate filter) {
        return of(filter == null ? Subjects.creaturesYouControl()
                : Subjects.controlledBy(Subjects.you(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), filter))));
    }

    @Override
    public TargetSpec targetSpec() {
        return subject instanceof PermanentSubject.Target
                ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
