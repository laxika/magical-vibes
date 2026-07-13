package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker (STATIC) effect for Rowen. Detected in {@code DrawService}: the controller reveals the
 * first card they draw each turn, and whenever that revealed card is a basic land card, a
 * "draw a card" triggered ability is put onto the stack. Only the turn's first draw is revealed;
 * the resulting extra draw is not.
 */
public record RevealFirstDrawDrawOnBasicLandEffect() implements CardEffect {
}
