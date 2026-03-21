package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered effect that exiles a permanent the controller controls or a card from their
 * hand or graveyard for each 1 life lost. Used by cards like Lich's Mastery.
 * The exile count is provided at trigger time via the trigger context, not stored in the effect.
 */
public record ExileForEachLifeLostEffect() implements CardEffect {
}
