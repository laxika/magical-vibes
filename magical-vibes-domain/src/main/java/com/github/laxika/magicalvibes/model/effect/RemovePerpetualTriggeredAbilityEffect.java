package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;

import java.util.Objects;

/**
 * Removes a matching perpetual triggered ability from the source card after a successful action.
 *
 * @param triggeredAbilityTrigger a trigger of the ability to remove
 * @param triggeredAbilityType    the class of one of its effects
 */
public record RemovePerpetualTriggeredAbilityEffect(
        Trigger triggeredAbilityTrigger, Class<? extends CardEffect> triggeredAbilityType) implements CardEffect {

    public RemovePerpetualTriggeredAbilityEffect {
        Objects.requireNonNull(triggeredAbilityTrigger, "triggeredAbilityTrigger");
        Objects.requireNonNull(triggeredAbilityType, "triggeredAbilityType");
    }

    /** Removes an ability still held in {@code slot}. */
    public RemovePerpetualTriggeredAbilityEffect(EffectSlot slot, Class<? extends CardEffect> triggeredAbilityType) {
        this(new LegacySlotTrigger(Objects.requireNonNull(slot, "slot")), triggeredAbilityType);
    }
}
