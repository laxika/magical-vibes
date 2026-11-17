package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * Grants the source permanent a triggered ability until its controller's next upkeep.
 *
 * <p>The grant is represented as a floating continuous effect so it survives turn cleanup and
 * expires at the correct upkeep boundary.
 */
public record GrantEffectToSourceUntilNextUpkeepEffect(
        Ability grantedAbility
) implements CardEffect {

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
