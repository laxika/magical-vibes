package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Progress state for Torment of Hailfire's "repeat X times: each opponent loses 3 life unless that
 * player sacrifices a nonland permanent or discards a card" flow. The flow is driven one opponent at
 * a time by {@code TormentOfHailfireEffectHandler}, which re-runs on each penalty choice and on each
 * discard/sacrifice sub-choice completion (via {@code rerunCurrentEffectAfterInteraction}).
 */
public class TormentState {

    /** Whether a flow is in progress (guards fresh initialization). */
    public boolean active;
    /** Whole iterations of the process still to perform once the current opponent queue drains. */
    public int remainingIterations;
    /** Opponents (APNAP order) not yet processed in the current iteration. */
    public final Deque<UUID> remaining = new ArrayDeque<>();
    /** The opponent currently making a penalty choice. */
    public UUID currentOpponentId;
    /** The penalty option the current opponent picked, consumed on the next re-entry. */
    public String chosenMode;

    public void reset() {
        active = false;
        remainingIterations = 0;
        remaining.clear();
        currentOpponentId = null;
        chosenMode = null;
    }
}
