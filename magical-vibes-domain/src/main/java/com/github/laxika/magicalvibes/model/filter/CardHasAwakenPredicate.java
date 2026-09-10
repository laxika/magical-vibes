package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards with an Awaken ability word. Scryfall exposes Awaken as a keyword, but it is an
 * ability word rather than a rules keyword and is therefore identified from the oracle text.
 */
public record CardHasAwakenPredicate() implements CardPredicate {
}
