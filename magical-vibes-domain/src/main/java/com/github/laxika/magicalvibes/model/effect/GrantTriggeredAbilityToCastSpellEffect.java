package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Grants the spell that caused this trigger a temporary triggered ability as it enters the
 * battlefield.
 */
public record GrantTriggeredAbilityToCastSpellEffect(EffectSlot slot, CardEffect grantedEffect)
        implements CardEffect {
}
