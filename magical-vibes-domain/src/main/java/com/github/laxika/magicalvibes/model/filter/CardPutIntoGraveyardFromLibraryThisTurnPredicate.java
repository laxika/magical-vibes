package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a non-token card in its owner's graveyard that was put there from a library this turn ("a Doctor card in your
 * graveyard that was put there from your library this turn"); read from
 * {@code GameData.cardsPutIntoGraveyardFromLibraryThisTurn}, which forgets a card once it leaves the graveyard.
 */
public record CardPutIntoGraveyardFromLibraryThisTurnPredicate() implements CardPredicate {
}
