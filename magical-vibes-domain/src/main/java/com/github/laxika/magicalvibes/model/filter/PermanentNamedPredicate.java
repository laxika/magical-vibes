package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents with the given name (exact string equality on {@code Card.getName()}).
 */
public record PermanentNamedPredicate(String cardName) implements PermanentPredicate {
}
