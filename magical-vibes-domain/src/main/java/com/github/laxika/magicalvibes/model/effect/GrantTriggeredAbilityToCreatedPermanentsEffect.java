package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * Gives every permanent created earlier in this same resolution (the {@code StackEntry.createdPermanentIds}
 * snapshot) an indefinite triggered ability: {@code grantedEffect} fires from {@code slot}, as if the
 * permanent had printed it. Tend to the Kiln's conjured Flamebraider uses it for "it gains haste and
 * 'At the beginning of the end step, sacrifice this creature.'"
 *
 * <p>The grant is stored on each permanent, so it outlives this resolution and is unaffected by the
 * granting trigger leaving the stack, and it follows the permanent's current controller.</p>
 */
public record GrantTriggeredAbilityToCreatedPermanentsEffect(EffectSlot slot, CardEffect grantedEffect)
        implements CardEffect {
}
