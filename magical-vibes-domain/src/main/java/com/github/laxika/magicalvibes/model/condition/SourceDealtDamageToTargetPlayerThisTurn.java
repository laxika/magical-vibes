package com.github.laxika.magicalvibes.model.condition;

/**
 * True when the source has dealt at least {@code minimum} damage this turn to "that player": the player a
 * damage-to-a-player ability's event was about, or the targeted player ("if it has dealt 10 or more damage to that
 * player this turn", Vessel of the All-Consuming). Combat and noncombat damage both count.
 */
public record SourceDealtDamageToTargetPlayerThisTurn(int minimum) implements Condition {

    public SourceDealtDamageToTargetPlayerThisTurn {
        if (minimum <= 0) {
            throw new IllegalArgumentException("minimum must be positive");
        }
    }

    @Override
    public String conditionName() {
        return "it has dealt " + minimum + " or more damage to that player this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "it has dealt less than " + minimum + " damage to that player this turn";
    }
}
