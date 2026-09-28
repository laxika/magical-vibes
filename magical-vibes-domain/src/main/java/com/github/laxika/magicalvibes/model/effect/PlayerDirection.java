package com.github.laxika.magicalvibes.model.effect;

/** Seating direction relative to the next player in turn order. */
public enum PlayerDirection {
    LEFT(1),
    RIGHT(-1);

    private final int turnOrderOffset;

    PlayerDirection(int turnOrderOffset) {
        this.turnOrderOffset = turnOrderOffset;
    }

    public int turnOrderOffset() {
        return turnOrderOffset;
    }
}
