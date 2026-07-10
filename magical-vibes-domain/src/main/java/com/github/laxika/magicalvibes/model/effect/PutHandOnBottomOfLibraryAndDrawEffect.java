package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player (e.g. the player whose draw step it is) puts the cards in their
 * hand on the bottom of their library in any order, then draws that many cards.
 * Used by Teferi's Puzzle Box.
 */
public record PutHandOnBottomOfLibraryAndDrawEffect() implements CardEffect {
}
