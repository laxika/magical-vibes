package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a non-token card in its owner's graveyard that was milled this turn ("cards in your graveyard that were
 * milled this turn", CR 701.17a); read from {@code GameData.cardsMilledThisTurn}, which forgets a card once it leaves
 * the graveyard. Needs the {@code GameData} overload and the card's owner.
 */
public record CardMilledThisTurnPredicate() implements CardPredicate {
}
