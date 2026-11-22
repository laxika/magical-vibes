package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Perpetually grants a triggered ability to a targeted creature, or to each creature of the set {@code subject} names as
 * the effect resolves ({@link Subjects#creaturesYouControl()}, Antique Collector: a card that already has it doesn't
 * get it again), which doesn't target.
 */
public record PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
        Ability grantedAbility,
        PermanentPredicate filter, PermanentSubject subject) implements CardEffect {

    /** The perpetual grant of {@code grantedAbility} to any target creature. */
    public PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(Ability grantedAbility) {
        this(grantedAbility, null, Subjects.target());
    }

    public PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(Ability grantedAbility, PermanentPredicate filter) {
        this(grantedAbility, filter, Subjects.target());
    }

    /** The perpetual grant to the creatures of a set. */
    public PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(Ability grantedAbility, PermanentSubject subject) {
        this(grantedAbility, null, subject);
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature(), filter) : TargetSpec.NONE;
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
