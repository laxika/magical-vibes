package com.github.laxika.magicalvibes.model.effect;

/**
 * Riot's as-enters choice: a creature enters with either a +1/+1 counter or haste.
 *
 * <p>This marker is placed in the static effect slot and resolved by the battlefield-entry and
 * may-ability services.
 */
public record RiotEffect() implements CardEffect {
}
