package com.github.laxika.magicalvibes.model.filter;

/** Matches cards whose name equals the name chosen by the source permanent. */
public record CardHasSourceChosenNamePredicate() implements CardPredicate {
}
