package com.github.laxika.magicalvibes.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable combat-declaration snapshot of one legal attack target.
 */
public record CombatAttackTarget(UUID id, String name, boolean isPlayer) {

    public CombatAttackTarget {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
