package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a card in a graveyard that may replace a draw by milling {@code millCount} cards and
 * returning itself to its owner's hand.
 */
public record DredgeEffect(int millCount) implements CardEffect {
}
