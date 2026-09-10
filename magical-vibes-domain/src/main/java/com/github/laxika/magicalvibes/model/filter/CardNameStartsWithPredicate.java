package com.github.laxika.magicalvibes.model.filter;

/** Matches cards whose name starts with the supplied text. */
public record CardNameStartsWithPredicate(String prefix) implements CardPredicate {
}
