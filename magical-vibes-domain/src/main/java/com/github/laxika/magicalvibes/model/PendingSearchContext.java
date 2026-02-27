package com.github.laxika.magicalvibes.model;

/**
 * Stores context for a pending Leonin Arbiter search tax MayAbility choice.
 */
public class PendingSearchContext {

    private final java.util.UUID playerId;
    private final int permanentIndex;

    public PendingSearchContext(java.util.UUID playerId, int permanentIndex) {
        this.playerId = playerId;
        this.permanentIndex = permanentIndex;
    }

    public java.util.UUID getPlayerId() {
        return playerId;
    }

    public int getPermanentIndex() {
        return permanentIndex;
    }
}
