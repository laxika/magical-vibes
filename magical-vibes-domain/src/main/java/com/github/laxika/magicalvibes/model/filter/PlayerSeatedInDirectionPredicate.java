package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PlayerDirection;

import java.util.Objects;

/**
 * Matches the player seated next to the effect's controller in {@code direction} ("the player to your right gains
 * control of this artifact", Hithlain Rope, Bucknard's Everfull Purse), read from the turn order (the player to your
 * left takes the next turn). With only one player in the game that is the controller.
 */
public record PlayerSeatedInDirectionPredicate(PlayerDirection direction) implements PlayerPredicate {

    public PlayerSeatedInDirectionPredicate {
        Objects.requireNonNull(direction, "direction");
    }
}
