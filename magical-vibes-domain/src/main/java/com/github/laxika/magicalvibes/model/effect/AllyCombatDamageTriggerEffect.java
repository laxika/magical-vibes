package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker effect for the ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER slot.
 * When a creature controlled by the same player deals combat damage to a player and matches
 * {@code dealerPredicate} (null = any creature), the wrapped {@code effect} is put on the stack
 * for the source's controller. Wrap {@code effect} in a {@link MayEffect} for "you may" wordings.
 * Used by Boggart Mob ("Whenever a Goblin you control deals combat damage to a player, you may
 * create a 1/1 black Goblin Rogue creature token.").
 */
public record AllyCombatDamageTriggerEffect(PermanentPredicate dealerPredicate, CardEffect effect) implements CardEffect {
}
