package com.github.laxika.magicalvibes.model.condition;

/**
 * True when a counter was put on the source permanent during the current turn.
 *
 * @param byAnyPlayer when {@code false} (the default), only a counter the controller put on the source
 *                    counts ("if you put a counter on this creature this turn"); when {@code true}, a
 *                    counter put on it by any player or effect counts ("if a counter was put on this
 *                    creature this turn")
 */
public record PutCounterOnSourceThisTurn(boolean byAnyPlayer) implements Condition {

    public PutCounterOnSourceThisTurn() {
        this(false);
    }

    @Override
    public String conditionName() {
        return "put a counter on this permanent this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return byAnyPlayer
                ? "no counter was put on this permanent this turn"
                : "you did not put a counter on this permanent this turn";
    }
}
