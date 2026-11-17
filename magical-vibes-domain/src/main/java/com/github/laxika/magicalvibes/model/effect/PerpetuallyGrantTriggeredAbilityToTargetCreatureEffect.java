package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Perpetually grants a triggered ability to a targeted creature. */
public record PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(
        Ability grantedAbility,
        PermanentPredicate filter) implements CardEffect {

    /** The perpetual grant of {@code grantedAbility} to any target creature. */
    public PerpetuallyGrantTriggeredAbilityToTargetCreatureEffect(Ability grantedAbility) {
        this(grantedAbility, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(), filter);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
