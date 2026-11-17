package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * When resolved, grants each targeted creature a triggered ability for the rest of the current
 * combat. The grant is removed when combat state is cleared.
 */
public record GrantEffectToTargetUntilEndOfCombatEffect(
        Ability grantedAbility
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
