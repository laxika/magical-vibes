package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Progress state for a single-pass "each player may pay any amount of mana; then each player
 * creates that many tokens" flow (Liege of the Hollows). The flow is driven one player at a time
 * by {@code EachPlayerPaysAnyManaForTokensEffectHandler}, which re-runs on each X-value choice.
 *
 * <p>Players are prompted once each, in APNAP order (CR 101.4): the active player chooses first,
 * then the remaining players in turn order. Unlike the life variant there is no repetition — once
 * every player has chosen, each creates one token per mana they paid.
 */
public class EachPlayerPayManaState {

    /** Whether a flow is in progress (guards fresh initialization). */
    public boolean active;
    /** APNAP prompt order, active player first. */
    public final List<UUID> order = new ArrayList<>();
    /** Pointer into {@link #order} for the player currently choosing. */
    public int index;
    /** Total mana paid per player; token count at the end. */
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
