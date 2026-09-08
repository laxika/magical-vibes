package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static replacement effect that exiles an opponent's creature instead of
 * allowing it to die, optionally with an effect that triggers after the exile.
 */
public interface OpponentCreatureDeathExileReplacement extends CardEffect {

    boolean nontokenOnly();

    CardEffect whenExiledEffect();
}
