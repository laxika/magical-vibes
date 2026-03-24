package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller of this effect wins the game.
 * Used as the wrapped effect inside conditional win conditions
 * (e.g. Revel in Riches: "if you control ten or more Treasures, you win the game").
 */
public record WinGameEffect() implements CardEffect {
}
