package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Progress state for Illicit Auction's life-bid auction. The flow is driven one bidder at a time by
 * {@code IllicitAuctionEffectHandler}, which re-runs on each bid answer (via
 * {@link GameData#rerunCurrentEffectAfterInteraction}).
 *
 * <p>{@link #order} is the turn-order rotation with the controller first; {@link #index} points at
 * the bidder currently being prompted. The controller opens as the {@link #highBidderId} with a
 * {@link #highBid} of 0. Each prompt advances {@code index} to the next player; if that player is the
 * high bidder again (the bid came all the way around with no raise), the auction ends. Any bid greater
 * than {@code highBid} becomes the new high bid and high bidder.
 */
public class IllicitAuctionState {

    /** Whether an auction is in progress (guards fresh initialization). */
    public boolean active;
    /** Turn order for the round-robin, controller first. */
    public final List<UUID> order = new ArrayList<>();
    /** Pointer into {@link #order} for the bidder currently being prompted. */
    public int index;
    /** The current high bid (starts at 0). */
    public int highBid;
    /** The player holding the high bid (starts as the controller). */
    public UUID highBidderId;
    /** The player currently being prompted to bid. */
    public UUID currentBidderId;

    public void reset() {
        active = false;
        order.clear();
        index = 0;
        highBid = 0;
        highBidderId = null;
        currentBidderId = null;
    }
}
