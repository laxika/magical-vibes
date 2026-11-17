package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * When resolved, grants each matching creature the controller currently controls a triggered
 * ability for the rest of the current combat. The optional filter is evaluated at resolution.
 */
public record GrantEffectToOwnCreaturesUntilEndOfCombatEffect(
        Ability grantedAbility,
        PermanentPredicate filter
) implements CardEffect {

    /** The grant of {@code grantedAbility}. */
    public GrantEffectToOwnCreaturesUntilEndOfCombatEffect(Ability grantedAbility) {
        this(grantedAbility, null);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
