package com.github.laxika.magicalvibes.model.filter;

/** Matches token cards. Wrap in {@link CardNotPredicate} for "nontoken". */
public record CardIsTokenPredicate() implements CardPredicate {
}
