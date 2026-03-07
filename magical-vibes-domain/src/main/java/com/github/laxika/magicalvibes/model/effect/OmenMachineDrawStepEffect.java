package com.github.laxika.magicalvibes.model.effect;

/**
 * Triggered ability: at the beginning of each player's draw step, that player exiles
 * the top card of their library. If it's a land card, the player puts it onto the
 * battlefield. Otherwise, the player casts it without paying its mana cost if able.
 */
public record OmenMachineDrawStepEffect() implements CardEffect {
}
