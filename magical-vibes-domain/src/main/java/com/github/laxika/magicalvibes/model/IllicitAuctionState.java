package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Progress state for life-bid auctions such as Illicit Auction and Mages' Contest. The flow is
 * driven one bidder at a time by the corresponding effect handler, which re-runs on each bid answer (via
 * {@link GameData#rerunCurrentEffectAfterInteraction}).
 *
 * <p>{@link #order} is the turn-order rotation with the opening bidder first; {@link #index} points at
 * the bidder currently being prompted. The opening bidder starts as the {@link #highBidderId} with
 * the effect-specific opening {@link #highBid}. Each prompt advances {@code index} to the next player; if that player is the
 * high bidder again (the bid came all the way around with no raise), the auction ends. Any bid greater
 * than {@code highBid} becomes the new high bid and high bidder.
 */
public class IllicitAuctionState {

    /** Whether an auction is in progress (guards fresh initialization). */
    public boolean active;
    /** Turn order for the round-robin, opening bidder first. */
    public final List<UUID> order = new ArrayList<>();
    /** Pointer into {@link #order} for the bidder currently being prompted. */
    public int index;
    /** The current high bid. */
    public int highBid;
    /** The player holding the high bid. */
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
