package com.github.laxika.magicalvibes.model.filter;

/**
 * "A card of that color": one of the color an earlier {@code ChooseColorAtResolutionEffect} of this resolution
 * chose, as effects leave the card's colors; needs the game state.
 */
public record CardHasChosenSpellColorPredicate() implements CardPredicate {
}
