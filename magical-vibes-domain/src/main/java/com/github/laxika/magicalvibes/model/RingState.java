package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** The Ring emblem's level and its current Ring-bearer for one player. */
public record RingState(int level, UUID bearerId) {

    public RingState {
        if (level < 1 || level > 4) {
            throw new IllegalArgumentException("The Ring level must be between 1 and 4");
        }
    }

    public RingState tempt(UUID newBearerId) {
        return new RingState(Math.min(4, level + 1), newBearerId);
    }
}
