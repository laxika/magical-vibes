package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card with the same name as one of the cards exiled with the source permanent: "whenever you play a card
 * with the same name as one of the exiled cards" (Search the City). Evaluated with the source, it reads the cards
 * {@code GameData} tracks as exiled with that permanent as it is judged; without a source permanent it matches
 * nothing.
 */
public record CardSharesNameWithCardExiledWithSourcePredicate() implements CardPredicate {
}
