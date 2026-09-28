package com.github.laxika.magicalvibes.model.effect;

/**
 * Flips coins until the controller loses a flip, optionally resolving a payload once for each won
 * flip.
 */
public record FlipUntilLoseEffect(CardEffect perWin) implements CardEffect {

}
