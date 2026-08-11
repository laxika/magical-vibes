package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent if it has a color that is tied for most common among all permanents.
 * Colorless permanents never match; each color of a multicolored permanent is considered.
 */
public record PermanentSharesMostCommonColorPredicate() implements PermanentPredicate {
}
