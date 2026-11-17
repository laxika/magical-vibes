package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/**
 * Registers a global triggered ability until end of turn. This is used for effects that watch an
 * event across all players' permanents and therefore cannot be stored on one permanent.
 */
public record RegisterGlobalTriggeredAbilityUntilEndOfTurnEffect(Ability grantedAbility)
        implements CardEffect {

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
