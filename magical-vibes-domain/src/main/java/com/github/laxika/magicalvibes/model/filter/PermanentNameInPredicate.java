package com.github.laxika.magicalvibes.model.filter;

import java.util.Set;

/**
 * Matches permanents whose name is one of {@code cardNames} (exact string equality on
 * {@code Card.getName()}).
 *
 * <p>The multi-name counterpart of {@link PermanentNamedPredicate}, for cards that refer to a fixed
 * roster of names rather than a single one — Apocalypse Chime's "a name originally printed in the
 * Homelands expansion".
 */
public record PermanentNameInPredicate(Set<String> cardNames) implements PermanentPredicate {
}
