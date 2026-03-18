package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: if the controller would draw a card while their library
 * has no cards in it, they win the game instead.
 */
public record WinGameOnEmptyLibraryDrawEffect() implements CardEffect {
}
