package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Progress state for a single-pass "each player may pay any amount of mana" flow. The flow is
 * driven one player at a time by its effect handler, which re-runs on each X-value choice.
 *
 * <p>Players are prompted once each in the order selected by the effect handler. Unlike the life
 * variant there is no repetition once every player has chosen.
 */
public class EachPlayerPayManaState {

    /** Whether a flow is in progress (guards fresh initialization). */
    public boolean active;
    /** Prompt order selected by the effect handler. */
    public final List<UUID> order = new ArrayList<>();
    /** Pointer into {@link #order} for the player currently choosing. */
    public int index;
    /** Total mana paid per player. */
    public final Map<UUID, Integer> manaPaid = new LinkedHashMap<>();
    /** The player currently choosing how much mana to pay. */
    public UUID currentPlayerId;
    /** Set code of the resolving source card, for token art preference. */
    public String sourceSetCode;

    public void reset() {
        active = false;
        order.clear();
        index = 0;
        manaPaid.clear();
        currentPlayerId = null;
        sourceSetCode = null;
    }
}
