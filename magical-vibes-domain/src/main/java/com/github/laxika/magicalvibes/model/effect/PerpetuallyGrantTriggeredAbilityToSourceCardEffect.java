package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/** Perpetually grants a triggered ability to the card that supplied the resolving trigger. */
public record PerpetuallyGrantTriggeredAbilityToSourceCardEffect(
        EffectSlot triggeredAbilitySlot,
        CardEffect triggeredAbility) implements CardEffect {
}
