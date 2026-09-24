package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/** Resolves to a global attack-target restriction lasting until the controller's next turn. */
public record RestrictAttacksToDirectionUntilNextTurnEffect(Direction direction)
        implements AttackDirectionRestrictionEffect {

    public RestrictAttacksToDirectionUntilNextTurnEffect {
        Objects.requireNonNull(direction, "direction");
    }

    public enum Direction {
        LEFT,
        RIGHT
    }
}
