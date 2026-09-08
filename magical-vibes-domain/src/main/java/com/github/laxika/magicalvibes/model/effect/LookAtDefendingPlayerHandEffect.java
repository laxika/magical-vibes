package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller looks at the defending player's hand from a combat trigger.
 *
 * <p>The defending player is determined by combat and is stored on the stack entry's
 * {@code attackedTargetId}; this effect does not choose a player target.</p>
 */
public record LookAtDefendingPlayerHandEffect() implements CardEffect {
}
