package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that is multicoloured, i.e. has two or more colours among its colours
 * ({@code Card.getColors()}). Colourless cards (zero colours) and monocoloured cards (exactly one
 * colour) do not match (CR 105.4). Unlike {@link PermanentIsMonocoloredPredicate}, this operates on
 * a {@link com.github.laxika.magicalvibes.model.Card} in any zone (used for graveyard-card filters
 * such as Reborn Hope's "target multicolored card from your graveyard").
 */
public record CardIsMulticoloredPredicate() implements CardPredicate {
}
