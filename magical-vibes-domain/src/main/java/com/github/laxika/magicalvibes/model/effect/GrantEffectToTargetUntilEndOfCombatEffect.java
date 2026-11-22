package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * When resolved, grants each targeted creature, or each permanent of the set {@code subject} names as the effect
 * resolves (CR 611.2c), a triggered ability for the rest of the current combat: a floating layer-6 effect on it that
 * ends as the combat phase ends. The ability's source is the granting permanent of what it grants.
 */
public record GrantEffectToTargetUntilEndOfCombatEffect(
        Ability grantedAbility,
        PermanentSubject subject
) implements CardEffect {

    public GrantEffectToTargetUntilEndOfCombatEffect {
        if (!subject.equals(Subjects.target()) && (!subject.isSet() || subject.declaresTarget())) {
            throw new IllegalArgumentException("Unsupported grant subject: " + subject);
        }
    }

    /** The grant of {@code grantedAbility} to the targets. */
    public GrantEffectToTargetUntilEndOfCombatEffect(Ability grantedAbility) {
        this(grantedAbility, Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
