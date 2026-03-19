package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges the controller's life total with the source creature's toughness.
 * Per CR 701.10e: the player gets a new life total equal to the creature's toughness,
 * and the creature's toughness simultaneously becomes the player's former life total
 * (as a layer 7b toughness-setting effect).
 * Uses {@code sourcePermanentId} on the stack entry to identify the creature.
 */
public record ExchangeLifeTotalWithToughnessEffect() implements CardEffect {
}
