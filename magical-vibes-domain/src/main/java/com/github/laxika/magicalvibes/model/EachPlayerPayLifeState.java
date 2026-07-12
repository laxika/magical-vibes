package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Progress state for a repeating "starting with you, each player may pay any amount of life;
 * repeat until no one pays; each player creates a token for each 1 life paid" flow
 * (Plague of Vermin). The flow is driven one player at a time by
 * {@code EachPlayerPaysAnyLifeForTokensEffectHandler}, which re-runs on each X-value choice.
 *
 * <p>Players are prompted round-robin in turn order starting with the controller. Each pass a
 * player pays 0 increments {@link #consecutivePasses}; any non-zero payment resets it. When a
 * full round of consecutive passes has elapsed ({@code consecutivePasses == order.size()}) the
 * process ends and each player creates that many tokens.
 */
public class EachPlayerPayLifeState {

    /** Whether a flow is in progress (guards fresh initialization). */
    public boolean active;
    /** Turn order for the round-robin, controller first. */
    public final List<UUID> order = new ArrayList<>();
    /** Pointer into {@link #order} for the player currently choosing. */
    public int index;
    /** Consecutive players (across the rolling round-robin) who have paid 0 life. */
    public int consecutivePasses;
    /** Total life paid so far per player; token count at the end. */
    public final Map<UUID, Integer> lifePaid = new LinkedHashMap<>();
    /** The player currently choosing how much life to pay. */
    public UUID currentPlayerId;

    public void reset() {
        active = false;
        order.clear();
        index = 0;
        consecutivePasses = 0;
        lifePaid.clear();
        currentPlayerId = null;
    }
}
