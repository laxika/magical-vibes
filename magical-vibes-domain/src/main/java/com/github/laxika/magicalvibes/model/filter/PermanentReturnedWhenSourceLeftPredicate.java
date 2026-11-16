package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/**
 * Permanents returned to the battlefield from the exile they were in "until" {@code sourcePermanentId} left the
 * battlefield, as it left: "an exiled card enters under your control this way" (Lagrella, the Magpie).
 *
 * @param sourcePermanentId the permanent whose leaving returned them
 */
public record PermanentReturnedWhenSourceLeftPredicate(UUID sourcePermanentId) implements PermanentPredicate {
}
