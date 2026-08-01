package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Carry-over state for "for each player, search that player's library for a nonland card and exile
 * it, then that player shuffles. You may cast those cards without paying their mana costs" (Jace,
 * Architect of Thought's −8).
 *
 * <p>The flow spans one library search per player plus the final free-cast choice, so — like
 * {@link PendingPileSeparation} — it waits on the unified interaction queue as a state carrier
 * rather than riding a single interaction record: each completed search polls it, appends the card
 * it exiled, and re-queues it with {@code remainingPlayerIds} advanced. It never becomes the active
 * interaction, so its {@code decidingPlayerId()} stays {@code null}.
 *
 * @param searcherId        the player performing every search (the ability's controller)
 * @param remainingPlayerIds the players whose libraries have not been searched yet, in APNAP order
 * @param exiledCardIds     the cards exiled so far, offered for free casting once the queue drains
 */
public record PendingEachPlayerLibraryExile(UUID searcherId, List<UUID> remainingPlayerIds,
                                            List<UUID> exiledCardIds) implements PendingInteraction {

    public PendingEachPlayerLibraryExile {
        remainingPlayerIds = List.copyOf(remainingPlayerIds);
        exiledCardIds = List.copyOf(exiledCardIds);
    }
}
