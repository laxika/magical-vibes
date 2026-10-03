package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top ten cards of the controller's library. Each opponent, starting with the next
 * opponent in turn order, chooses a different nonland card; the chosen cards go into the
 * controller's hand and the rest go on the bottom of the library in a random order.
 */
public record ManifoldInsightsEffect() implements CardEffect {
}
