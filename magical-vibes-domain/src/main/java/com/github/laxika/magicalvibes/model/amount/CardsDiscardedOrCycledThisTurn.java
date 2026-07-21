package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of cards the controller has discarded this turn (cycling is a discard, CR 702.29e),
 * read from {@code GameData.cardsDiscardedThisTurn}. Used by Hollow One ("costs {2} less to cast
 * for each card you've cycled or discarded this turn").
 */
public record CardsDiscardedOrCycledThisTurn() implements DynamicAmount {
}
