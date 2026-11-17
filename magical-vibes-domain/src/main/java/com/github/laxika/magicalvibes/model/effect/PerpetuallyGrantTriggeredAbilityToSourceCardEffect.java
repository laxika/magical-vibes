package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.AbilityEffects;

/** Perpetually grants a triggered ability to the card that supplied the resolving trigger. */
public record PerpetuallyGrantTriggeredAbilityToSourceCardEffect(
        Ability grantedAbility) implements CardEffect {

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
