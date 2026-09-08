package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * When resolved, grants the source permanent a triggered ability indefinitely.
 *
 * @param slot the trigger slot to grant
 * @param grantedEffect the effect to fire when the trigger condition is met
 */
public record GrantEffectToSourceEffect(EffectSlot slot, CardEffect grantedEffect) implements CardEffect {
}
