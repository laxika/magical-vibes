package com.github.laxika.magicalvibes.model.effect;

/** Lets the controller choose up to a number of cards they own from outside the game to shuffle into their library. */
public record ShuffleUpToNCardsFromOutsideGameIntoLibraryEffect(int maxCount) implements CardEffect {
}
