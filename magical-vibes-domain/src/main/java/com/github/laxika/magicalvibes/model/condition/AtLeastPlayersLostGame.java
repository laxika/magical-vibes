package com.github.laxika.magicalvibes.model.condition;

/** True when at least {@code minimum} players have lost a game. */
public record AtLeastPlayersLostGame(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "at least " + minimum + " players have lost the game";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " players have lost the game";
    }
}
