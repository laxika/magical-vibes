package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * "Put a {@code counterType} counter on target creature for each {@code counterType} counter on this creature", the
 * effect of a "when this creature dies" ability (Grief Tyrant, which enters with four -1/-1 counters and hands them out
 * on death), and of modular's triggered ability ({@link #modularAbility()}).
 * <p>
 * The counters are counted on the stack entry's triggering permanent, as it last existed on the battlefield
 * (CR 603.10a): the source is off the battlefield by the time this resolves. A "you may" (Soulstinger, modular) is a
 * {@link MayEffect} around this effect, written on the card; the target is still chosen as the ability goes on the
 * stack (CR 603.3d) and the choice is made as it resolves (CR 603.5).
 *
 * @param counterType     the counter type counted on the dying creature and placed on the target
 * @param targetPredicate optional structural restriction for the target; {@code null} means any creature
 * @param modular         whether this is modular's triggered ability (CR 702.43a), which replacement effects that
 *                        modify "a modular triggered ability" read (Zabaz, the Glimmerwasp)
 */
public record PutCounterOnTargetForEachDyingSourceCounterEffect(
        CounterType counterType,
        PermanentPredicate targetPredicate,
        boolean modular
) implements CardEffect {

    /** The accept/decline prompt of modular's "you may" ({@link #modularAbility()}). */
    public static final String MODULAR_PROMPT =
            "Put a +1/+1 counter on target artifact creature for each +1/+1 counter on this permanent?";

    public PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType) {
        this(counterType, null, false);
    }

    public PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType,
                                                             PermanentPredicate targetPredicate) {
        this(counterType, targetPredicate, false);
    }

    /**
     * Modular's triggered ability without its "you may" (CR 702.43a): "put a +1/+1 counter on target artifact creature
     * for each +1/+1 counter on this permanent". Cards write it as
     * {@code new MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect.modularAbility(), MODULAR_PROMPT)}.
     */
    public static PutCounterOnTargetForEachDyingSourceCounterEffect modularAbility() {
        return new PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE,
                new PermanentAllOfPredicate(List.of(new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())),
                true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                targetPredicate != null ? targetPredicate : new PermanentIsCreaturePredicate());
    }
}
