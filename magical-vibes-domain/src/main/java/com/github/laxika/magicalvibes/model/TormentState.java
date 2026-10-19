package com.github.laxika.magicalvibes.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    /** Whether choices are collected first and carried out together at the end. */
    public boolean deferred;
    /** Whether each repetition of the process collects every opponent's choice before applying any. */
    public boolean deferEachIteration;
    /** Whether the process affects the spell's target instead of each opponent. */
    public boolean targetedFlow;
    /** Passes each opponent still has to make in deferred mode, counting the one in progress. */
    public int opponentPassesLeft;
    /** Passes each opponent makes in deferred mode. */
    public int totalPasses;
    /** Permanents chosen to be sacrificed in deferred mode. */
    public final List<UUID> chosenSacrifices = new ArrayList<>();
    /** Times each player loses life in deferred mode. */
    public final Map<UUID, Integer> lifeLossCounts = new HashMap<>();

    public void reset() {
        deferred = false;
        deferEachIteration = false;
        targetedFlow = false;
        opponentPassesLeft = 0;
        totalPasses = 0;
        chosenSacrifices.clear();
        lifeLossCounts.clear();
        active = false;
        remainingIterations = 0;
        remaining.clear();
        currentOpponentId = null;
        chosenMode = null;
    }
}
