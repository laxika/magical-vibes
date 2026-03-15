package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever you cast a spell from your graveyard" abilities.
 * <p>
 * Used in the {@code ON_CONTROLLER_CASTS_SPELL} slot. The collector checks
 * {@code TriggerContext.SpellCast.castFromHand() == false} to determine eligibility.
 *
 * @param resolvedEffects effects to put on the stack when this triggers
 */
public record CastFromGraveyardTriggerEffect(
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
