package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Combat trigger: destroy the combat opponent if it has the required subtype.
 * It can't be regenerated when cannotBeRegenerated is true.
 * Used on equipment with "blocks or becomes blocked by [subtype]" triggers (e.g. Wooden Stake).
 * <p>
 * At trigger creation time, this effect is checked against the combat opponent's subtype.
 * If the opponent has the required subtype, a {@link DestroyTargetPermanentEffect} is placed on the
 * stack instead, auto-targeting the opponent. If the opponent does not match, the trigger is skipped.
 *
 * @param requiredSubtype    the subtype the combat opponent must have for the trigger to fire
 * @param cannotBeRegenerated whether the destruction prevents regeneration
 */
public record DestroySubtypeCombatOpponentEffect(
        CardSubtype requiredSubtype,
        boolean cannotBeRegenerated
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
