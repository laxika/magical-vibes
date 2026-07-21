package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that is monocolored, i.e. has exactly one colour among its effective colours
 * ({@code Card.getColors()} as modified by the CR 613 layer system). Colourless permanents (zero
 * colours) and multicoloured permanents (two or more colours) do not match. Used by "a monocolored
 * creature" filters (Defiler of Souls).
 */
public record PermanentIsMonocoloredPredicate() implements PermanentPredicate {
}
