package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Progress state for a single-pass "each player may pay any amount of mana" flow. Token and draw
 * effects drive it one player at a time and re-run on each X-value choice.
 *
 * <p>Players are prompted once each in an effect-defined order, followed by the remaining players
 * in turn order. Unlike the life variant there is no repetition — once every player has chosen,
 * the resolving effect applies its result to the recorded payments.
 */
public class EachPlayerPayManaState {

    /** Whether a flow is in progress (guards fresh initialization). */
    public boolean active;
    /** Prompt order selected by the resolving effect. */
    public final List<UUID> order = new ArrayList<>();
    /** Pointer into {@link #order} for the player currently choosing. */
    public int index;
    /** Total mana paid per player; used to derive the resolving effect's result. */
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
