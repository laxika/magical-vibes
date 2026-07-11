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
 * <p>
 * When {@code creatureSpellOnly} is true the triggering spell must be a creature spell of the
 * chosen type (Vanquisher's Banner). When false any spell of the chosen type triggers, including
 * non-creature Tribal spells (Door of Destinies: "whenever you cast a spell of the chosen type").
 *
 * @param resolvedEffects   effects to put on the stack when this triggers
 * @param creatureSpellOnly whether the triggering spell must also be a creature spell
 */
public record ChosenSubtypeSpellCastTriggerEffect(
        List<CardEffect> resolvedEffects,
        boolean creatureSpellOnly
) implements CardEffect {

    public ChosenSubtypeSpellCastTriggerEffect(List<CardEffect> resolvedEffects) {
        this(resolvedEffects, true);
    }
}
