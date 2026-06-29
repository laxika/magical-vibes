package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches cards with the given name (exact string equality on {@code Card.getName()}).
 */
public record CardNamedPredicate(String cardName) implements CardPredicate {
}
