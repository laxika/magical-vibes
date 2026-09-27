package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell stack entry whose exact card identity is currently exiled with the source
 * permanent. This models "cast this way" without treating another copy with the same name as
 * the tracked card.
 */
public record StackEntryIsCardExiledWithSourcePredicate() implements StackEntryPredicate {
}
