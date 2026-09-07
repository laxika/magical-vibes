package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveals the top ten cards of the controller's library and lets them choose one card for each
 * represented two-color pair. The chosen cards go to hand and the rest go on the bottom of the
 * library in a random order.
 */
public record RevealTopTenCardsForColorPairsEffect() implements CardEffect {
}
