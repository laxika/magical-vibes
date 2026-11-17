package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * Grants the source permanent a temporary triggered ability whose event subject must match the
 * color chosen earlier during the same resolution.
 */
public record GrantEffectToSourceOfChosenColorUntilEndOfTurnEffect(
        Ability grantedAbility
) implements CardEffect {

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
