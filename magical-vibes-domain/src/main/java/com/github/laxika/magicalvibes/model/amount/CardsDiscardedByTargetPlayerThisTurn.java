package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of cards the targeted player has discarded this turn, read from
 * {@code GameData.cardsDiscardedThisTurn}. The target player's id comes from the stack entry's
 * target channel. Used by Dream Salvage.
 */
public record CardsDiscardedByTargetPlayerThisTurn() implements DynamicAmount {
}
