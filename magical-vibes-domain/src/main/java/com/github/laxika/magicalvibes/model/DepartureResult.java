package com.github.laxika.magicalvibes.model;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Where one {@link DepartureIntent}'s permanent ended up once its {@link DepartureBatch} was performed.
 * Callers read this instead of a {@code boolean} "was it removed".
 *
 * @param permanentId        the permanent the intent named
 * @param destination        the zone it ended up in ({@link Zone#BATTLEFIELD} if it stayed, for example
 *                           because it regenerated or is indestructible), or {@code null} if it is in no
 *                           zone (it was already gone, or a token ceased to exist)
 * @param destinationOwnerId the player whose graveyard, hand, library or exile it went to, or {@code null}
 * @param newCardId          its card's id in the new zone, or {@code null} when it didn't arrive in one
 * @param lastKnownPower     its power immediately before the event, or {@code null} if it wasn't a creature
 * @param lastKnownToughness its toughness immediately before the event, or {@code null} if it wasn't a creature
 * @param destroyed          whether it left the battlefield because it was destroyed
 * @param sacrificed         whether it left the battlefield because it was sacrificed
 * @param lastKnownControllerId the player who controlled it immediately before the event (CR 608.2h), or
 *                           {@code null} if it wasn't on the battlefield
 * @param lastKnownCardTypes the card types it had immediately before the event (CR 608.2h), or empty when the
 *                           event couldn't touch it
 */
public record DepartureResult(UUID permanentId, Zone destination, UUID destinationOwnerId, UUID newCardId,
                              Integer lastKnownPower, Integer lastKnownToughness,
                              boolean destroyed, boolean sacrificed, UUID lastKnownControllerId,
                              Set<CardType> lastKnownCardTypes) {

    public DepartureResult {
        Objects.requireNonNull(permanentId, "permanentId");
        lastKnownCardTypes = lastKnownCardTypes == null ? Set.of() : Set.copyOf(lastKnownCardTypes);
    }

    /** A result without last-known card types. */
    public DepartureResult(UUID permanentId, Zone destination, UUID destinationOwnerId, UUID newCardId,
                           Integer lastKnownPower, Integer lastKnownToughness,
                           boolean destroyed, boolean sacrificed, UUID lastKnownControllerId) {
        this(permanentId, destination, destinationOwnerId, newCardId, lastKnownPower, lastKnownToughness,
                destroyed, sacrificed, lastKnownControllerId, Set.of());
    }

    /** Whether it left the battlefield. */
    public boolean left() {
        return destination != Zone.BATTLEFIELD;
    }

    /** Whether it was put into a graveyard. */
    public boolean wentToGraveyard() {
        return destination == Zone.GRAVEYARD;
    }
}
