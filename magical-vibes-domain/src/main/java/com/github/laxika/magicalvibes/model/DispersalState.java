package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Progress state for Dispersal's opponent-by-opponent return-then-discard sequence. */
public class DispersalState {

    /** Whether a Dispersal sequence is in progress. */
    public boolean active;
    /** Opponents still to process in APNAP order. */
    public final Deque<UUID> remainingOpponentIds = new ArrayDeque<>();
    /** Opponent currently processing a permanent and discard. */
    public UUID currentOpponentId;
    /** Permanent selected automatically or by the current opponent's tie-break choice. */
    public UUID selectedPermanentId;
    /** Whether the current opponent's discard choice is awaiting completion. */
    public boolean awaitingDiscard;

    public void reset() {
        active = false;
        remainingOpponentIds.clear();
        currentOpponentId = null;
        selectedPermanentId = null;
        awaitingDiscard = false;
    }
}
