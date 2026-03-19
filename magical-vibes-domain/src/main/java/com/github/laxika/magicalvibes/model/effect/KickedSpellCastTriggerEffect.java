package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever you cast a spell, if that spell was kicked" abilities.
 * <p>
 * Works in the {@code ON_CONTROLLER_CASTS_SPELL} slot. The trigger collector checks the
 * stack to verify the cast spell was kicked before firing the resolved effects.
 *
 * @param resolvedEffects effects to put on the stack when a kicked spell is cast
 */
public record KickedSpellCastTriggerEffect(
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
