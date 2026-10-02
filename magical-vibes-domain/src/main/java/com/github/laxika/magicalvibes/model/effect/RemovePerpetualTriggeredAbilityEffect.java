package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.Objects;

/** Removes a matching perpetual triggered ability from the source card after a successful action. */
public record RemovePerpetualTriggeredAbilityEffect(
        EffectSlot triggeredAbilitySlot, Class<? extends CardEffect> triggeredAbilityType) implements CardEffect {

    public RemovePerpetualTriggeredAbilityEffect {
        Objects.requireNonNull(triggeredAbilitySlot, "triggeredAbilitySlot");
        Objects.requireNonNull(triggeredAbilityType, "triggeredAbilityType");
    }
}
