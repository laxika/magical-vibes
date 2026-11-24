package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One player's Ring emblem (CR 701.54c): how many times the Ring has tempted them, counted up to 4 (the most its
 * abilities read), and their Ring-bearer (CR 701.54a), or {@code null} when they have none. Kept in the one store
 * {@link GameData#ringStates}.
 */
public record RingState(int level, UUID bearerId) {

    public RingState {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("The Ring level must be between 1 and 4");
        }
    }

    /** The emblem after the Ring tempts its player once more, the Ring-bearer unchanged. */
    public RingState tempted() {
        return new RingState(Math.min(4, level + 1), bearerId);
    }

    /** The emblem with {@code newBearerId} as the Ring-bearer ({@code null}: none). */
    public RingState withBearer(UUID newBearerId) {
        return new RingState(level, newBearerId);
    }
}
