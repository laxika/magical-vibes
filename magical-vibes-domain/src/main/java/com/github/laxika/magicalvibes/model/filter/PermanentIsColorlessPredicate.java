package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that is colourless, i.e. has zero colours among its effective colours
 * ({@code Card.getColors()} as modified by the CR 613 layer system). Monocoloured and multicoloured
 * permanents do not match. The zero-colour counterpart of {@link PermanentIsMonocoloredPredicate}
 * and {@link PermanentIsMulticoloredPredicate}. Used by "target colorless creature" filters
 * (Infernal Reckoning).
 */
public record PermanentIsColorlessPredicate() implements PermanentPredicate {
}
