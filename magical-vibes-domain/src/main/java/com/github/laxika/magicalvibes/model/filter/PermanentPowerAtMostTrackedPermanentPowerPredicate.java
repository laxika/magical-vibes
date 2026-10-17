package com.github.laxika.magicalvibes.model.filter;

import java.util.UUID;

/**
 * Matches permanents whose effective power is at most the current power of the permanent with id
 * {@code trackedPermanentId}, re-read each time the predicate is evaluated. When the tracked
 * permanent has left the battlefield its last known power is used (CR 608.2h). Used by Nihiloor's
 * "power less than or equal to the tapped creature's power", which is checked both when the
 * reflexive ability is put on the stack and again when it resolves.
 */
public record PermanentPowerAtMostTrackedPermanentPowerPredicate(UUID trackedPermanentId)
        implements PermanentPredicate {
}
