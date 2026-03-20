package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges the controller's life total with the source creature's power or toughness.
 * Per CR 701.10e: the player gets a new life total equal to the creature's stat,
 * and the creature's stat simultaneously becomes the player's former life total
 * (as a layer 7b setting effect).
 * Uses {@code sourcePermanentId} on the stack entry to identify the creature.
 */
public record ExchangeLifeTotalWithCreatureStatEffect(Stat stat) implements CardEffect {

    public enum Stat {
        POWER, TOUGHNESS
    }
}
