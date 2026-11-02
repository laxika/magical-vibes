package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever the first noncreature spell of a turn is cast".
 * The trigger is evaluated globally across all players and is intended for the
 * {@code Triggers.playerCastsSpell()} ability.
 *
 * @param resolvedEffects effects to put on the stack when the trigger fires
 */
public record FirstNoncreatureSpellCastTriggerEffect(List<CardEffect> resolvedEffects) implements CardEffect {
}
