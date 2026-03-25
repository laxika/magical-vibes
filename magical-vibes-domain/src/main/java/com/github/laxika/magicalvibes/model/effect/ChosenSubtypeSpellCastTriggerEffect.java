package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever you cast a creature spell of the chosen type" abilities.
 * <p>
 * The chosen type is read at trigger-check time from the source permanent's
 * {@code chosenSubtype} field, so it works with the {@link ChooseSubtypeOnEnterEffect}
 * replacement effect.
 * <p>
 * Place this on the {@code ON_CONTROLLER_CASTS_SPELL} slot.
 *
 * @param resolvedEffects effects to put on the stack when this triggers
 */
public record ChosenSubtypeSpellCastTriggerEffect(
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
