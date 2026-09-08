package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever a player casts a spell, if that spell was kicked" abilities.
 * <p>
 * Works in the {@code ON_ANY_PLAYER_CASTS_SPELL} and {@code ON_CONTROLLER_CASTS_SPELL} slots.
 * The trigger collector checks the stack to verify the cast spell was kicked before firing the
 * resolved effects. Multikicker payments are counted individually when the spell declares its
 * repeatable cost as multikicker; the count is available to resolved effects through the trigger
 * entry's event-value channel.
 *
 * @param resolvedEffects effects to put on the stack when a kicked spell is cast
 */
public record KickedSpellCastTriggerEffect(
        List<CardEffect> resolvedEffects
) implements CardEffect {
}
