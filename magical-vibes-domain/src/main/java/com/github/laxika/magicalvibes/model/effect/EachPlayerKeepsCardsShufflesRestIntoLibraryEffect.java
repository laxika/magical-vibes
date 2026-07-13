package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses up to {@code keepCount} cards in their hand to keep, then shuffles the
 * rest into their library. Players choose in APNAP order (active player first). Used by
 * Worldpurge.
 */
public record EachPlayerKeepsCardsShufflesRestIntoLibraryEffect(int keepCount) implements CardEffect {
}
