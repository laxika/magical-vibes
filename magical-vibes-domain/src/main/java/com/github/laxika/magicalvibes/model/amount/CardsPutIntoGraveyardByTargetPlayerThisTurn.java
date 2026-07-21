package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of cards put into the targeted player's graveyard from anywhere this turn, read from
 * {@code GameData.cardsPutIntoGraveyardFromAnywhereThisTurn}. Tokens are excluded (a token is not a
 * card), and cards that were put into the graveyard and later left it are still counted (the set is
 * only cleared at turn start). The target player's id comes from the stack entry's target channel.
 * Used by Fraying Sanity.
 */
public record CardsPutIntoGraveyardByTargetPlayerThisTurn() implements DynamicAmount {
}
