package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/** Perpetually grants a triggered ability to the creatures currently controlled by the source. */
public record PerpetuallyGrantTriggeredAbilityToOwnCreaturesEffect(
        EffectSlot triggeredAbilitySlot,
        CardEffect triggeredAbility) implements CardEffect {
}
