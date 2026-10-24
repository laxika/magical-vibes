package com.github.laxika.magicalvibes.model;

import java.util.Objects;
import java.util.UUID;

/**
 * One permanent a {@link DepartureBatch} moves off the battlefield. The permanent is referenced by
 * id, so a simulation copy of the batch resolves it against the copy's own battlefield.
 *
 * @param permanentId        the departing permanent
 * @param cause              why it leaves
 * @param graveyardOwnerId   the player whose graveyard is its default destination, or {@code null} for
 *                           its owner's graveyard
 * @param cantBeRegenerated  whether the destruction says it can't be regenerated (Wrath of God), so
 *                           regeneration is not among its destruction replacements
 * @param destructionReplacementsApplied whether its caller has already applied indestructible and the
 *                           destruction replacements (CR 614.8), so the batch doesn't gather them again
 */
public record DepartureIntent(UUID permanentId, DepartureCause cause, UUID graveyardOwnerId,
                              boolean cantBeRegenerated, boolean destructionReplacementsApplied) {

    public DepartureIntent {
        Objects.requireNonNull(permanentId, "permanentId");
        Objects.requireNonNull(cause, "cause");
    }

    /** A departure to its owner's graveyard. */
    public DepartureIntent(UUID permanentId, DepartureCause cause) {
        this(permanentId, cause, null, false, false);
    }

    /** A departure to {@code graveyardOwnerId}'s graveyard, or its owner's when {@code null}. */
    public DepartureIntent(UUID permanentId, DepartureCause cause, UUID graveyardOwnerId) {
        this(permanentId, cause, graveyardOwnerId, false, false);
    }

    /** A destruction by a spell or ability (CR 701.8a); {@code cantBeRegenerated} as the effect says. */
    public static DepartureIntent destroy(UUID permanentId, boolean cantBeRegenerated) {
        return new DepartureIntent(permanentId, DepartureCause.DESTROY, null, cantBeRegenerated, false);
    }

    /**
     * A destruction by a spell or ability (CR 701.8a) whose indestructible and destruction replacements its caller
     * has already applied: it only moves the permanent and settles its death replacements.
     */
    public static DepartureIntent destroyAfterDestructionReplacements(UUID permanentId) {
        return new DepartureIntent(permanentId, DepartureCause.DESTROY, null, false, true);
    }

    /** A sacrifice by its controller (CR 701.21a). */
    public static DepartureIntent sacrifice(UUID permanentId) {
        return new DepartureIntent(permanentId, DepartureCause.SACRIFICE);
    }
}
