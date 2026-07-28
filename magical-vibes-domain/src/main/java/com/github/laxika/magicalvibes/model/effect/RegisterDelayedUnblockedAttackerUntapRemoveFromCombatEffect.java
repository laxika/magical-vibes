package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability for the rest of the combat: whenever a
 * creature attacks and isn't blocked this combat, untap it and remove it from combat. Applies to
 * every unblocked attacker, not only the controller's.
 *
 * <p>Registered by Melee. See {@code DelayedUnblockedAttackerUntapRemoveFromCombat}.
 */
public record RegisterDelayedUnblockedAttackerUntapRemoveFromCombatEffect() implements CardEffect {
}
