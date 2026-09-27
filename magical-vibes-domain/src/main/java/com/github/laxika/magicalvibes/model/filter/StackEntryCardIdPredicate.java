package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/** Matches a spell whose physical card has the given identity. */
public record StackEntryCardIdPredicate(UUID cardId) implements StackEntryPredicate {
}
