package com.github.laxika.magicalvibes.model.filter;

/** Matches cards whose colors contain exactly the specified number of colors. */
public record CardHasExactlyNColorsPredicate(int colorCount) implements CardPredicate {
}
