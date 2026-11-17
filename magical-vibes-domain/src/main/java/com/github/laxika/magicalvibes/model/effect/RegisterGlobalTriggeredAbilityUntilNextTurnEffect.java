package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/** Registers a global triggered ability until the beginning of the controller's next turn. */
public record RegisterGlobalTriggeredAbilityUntilNextTurnEffect(Ability grantedAbility)
        implements CardEffect {

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredEffect() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
