package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a colorless card — one with an empty colour list ({@code Card.getColors()}).
 * Artefacts, Eldrazi, and other cards with no WUBRG colours all match (CR 105.2).
 */
public record CardIsColorlessPredicate() implements CardPredicate {
}
