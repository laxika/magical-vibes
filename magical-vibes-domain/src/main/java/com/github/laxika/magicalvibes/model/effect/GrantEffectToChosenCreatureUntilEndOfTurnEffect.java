package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/** Grants a temporary triggered ability to the creature remembered by the resolving entry. */
public record GrantEffectToChosenCreatureUntilEndOfTurnEffect(
        EffectSlot slot,
        CardEffect grantedEffect
) implements CardEffect {
}
