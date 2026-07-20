package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Remove a counter from target permanent."
 *
 * <p>When {@code counterType} is {@code null}, removes a single counter of any one kind currently on
 * the target (if several kinds are present, one of the first present kind). No-op when the target has
 * no counters. Used by Medicine Runner.</p>
 *
 * <p>When {@code counterType} is set, removes a single counter of exactly that type ("remove a -1/-1
 * counter from target creature"); no-op when the target carries none of that type. Used by Defiant
 * Greatmaw ({@code MINUS_ONE_MINUS_ONE}).</p>
 *
 * <p>{@code targetPredicate} narrows which permanents are legal targets ({@code null} = any permanent);
 * e.g. Defiant Greatmaw restricts to "another creature you control".</p>
 */
public record RemoveCounterFromTargetPermanentEffect(CounterType counterType,
                                                     PermanentPredicate targetPredicate) implements CardEffect {

    /** "Remove a counter from target permanent" — any kind, any permanent (Medicine Runner). */
    public RemoveCounterFromTargetPermanentEffect() {
        this(null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate != null
                ? TargetSpec.benign(TargetCategory.CREATURE, targetPredicate)
                : TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
