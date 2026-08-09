package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that lets a spell's controller discard a card sharing a color with that spell
 * rather than pay its mana cost.
 */
public record SharedColorDiscardAlternativeCostEffect() implements CardEffect {
}
