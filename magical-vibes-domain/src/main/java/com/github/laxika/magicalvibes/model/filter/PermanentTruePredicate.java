package com.github.laxika.magicalvibes.model.filter;

/**
 * A {@link PermanentPredicate} that matches any permanent unconditionally.
 * Use this when an effect applies to all permanents regardless of type,
 * color, or other characteristics (e.g. "sacrifice a permanent").
 */
public record PermanentTruePredicate() implements PermanentPredicate {
}
