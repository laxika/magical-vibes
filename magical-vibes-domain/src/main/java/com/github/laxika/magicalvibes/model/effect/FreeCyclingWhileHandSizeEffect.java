package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect (New Perspectives): "As long as you have seven or more cards in hand, you may pay
 * {0} rather than pay cycling costs." Added as a static ability; consulted at cycling
 * activation via the {@link FreeCyclingEffect} fact.
 */
public record FreeCyclingWhileHandSizeEffect(int minCardsInHand) implements FreeCyclingEffect {
}
