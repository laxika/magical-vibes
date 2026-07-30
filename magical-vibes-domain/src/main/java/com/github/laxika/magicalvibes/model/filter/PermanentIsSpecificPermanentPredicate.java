package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/**
 * Matches exactly one permanent by id. Used when an effect that stores a {@link PermanentPredicate}
 * has to be narrowed at resolution time to a chosen target (Terrifying Presence).
 *
 * @param permanentId the id of the only matching permanent
 */
public record PermanentIsSpecificPermanentPredicate(UUID permanentId) implements PermanentPredicate {
}
