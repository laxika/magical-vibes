package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller has been dealt at least {@code minimumAmount} damage this turn (from any source,
 * combat or not). The controller-relative counterpart of {@link OpponentDealtDamageThisTurn};
 * both read {@code GameData.damageDealtToPlayersThisTurn}. Boarded Window uses {@code 4} for
 * "if you were dealt 4 or more damage this turn".
 */
public record ControllerDealtDamageThisTurn(int minimumAmount) implements Condition {

    @Override
    public String conditionName() {
        return minimumAmount <= 1
                ? "you were dealt damage this turn"
                : "you were dealt " + minimumAmount + " or more damage this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimumAmount <= 1
                ? "you were not dealt damage this turn"
                : "you were not dealt " + minimumAmount + " or more damage this turn";
    }
}
