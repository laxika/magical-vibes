package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Trigger descriptor for "whenever a player casts their first multicolored spell each turn".
 * The trigger is evaluated independently for each casting player and is intended for the
 * {@code Triggers.playerCastsSpell()} ability.
 *
 * @param resolvedEffects effects to put on the stack when the trigger fires
 */
public record FirstMulticoloredSpellCastTriggerEffect(List<CardEffect> resolvedEffects) implements CardEffect {
}
