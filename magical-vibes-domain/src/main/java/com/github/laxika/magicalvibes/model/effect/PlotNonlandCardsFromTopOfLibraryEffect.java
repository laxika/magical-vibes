package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission effect: the controller may plot nonland cards from the top of their library.
 * The plot cost is taken from the top card's mana cost at the time the special action is taken.
 */
public record PlotNonlandCardsFromTopOfLibraryEffect() implements CardEffect {
}
