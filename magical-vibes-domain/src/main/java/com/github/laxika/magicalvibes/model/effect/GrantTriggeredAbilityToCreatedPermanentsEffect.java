package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LegacySlots;

/**
 * Gives every permanent created earlier in this same resolution (the {@code StackEntry.createdPermanentIds}
 * snapshot) an indefinite triggered ability: {@code grantedEffect} fires from {@code slot}, as if the
 * permanent had printed it. Tend to the Kiln's conjured Flamebraider uses it for "it gains haste and
 * 'At the beginning of the end step, sacrifice this creature.'"
 *
 * <p>The grant is stored on each permanent, so it outlives this resolution and is unaffected by the
 * granting trigger leaving the stack, and it follows the permanent's current controller.</p>
 */
public record GrantTriggeredAbilityToCreatedPermanentsEffect(Ability grantedAbility)
        implements CardEffect {

    /** The grant as the slot and effect that stand for it. */
    public GrantTriggeredAbilityToCreatedPermanentsEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(LegacySlots.grant(slot, grantedEffect));
    }

    /** The slot of {@link #grantedAbility}, or {@code null} when there is none. */
    public EffectSlot slot() {
        return grantedAbility == null ? null : LegacySlots.slotOf(grantedAbility);
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect grantedEffect() {
        return grantedAbility == null ? null : LegacySlots.effectsOf(grantedAbility).getFirst();
    }
}
