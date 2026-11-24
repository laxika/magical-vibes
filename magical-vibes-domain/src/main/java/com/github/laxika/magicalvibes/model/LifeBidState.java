package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Progress of the life-bid step ({@code BidLifeEffect}) while it waits for a bid. Its handler resolves again on each
 * answered bid ({@link GameData#rerunCurrentEffectAfterInteraction}) and changes nothing else until the high bid stands.
 *
 * <p>{@link #order} is the bidding order, the controller first and the other bidders in turn order after them;
 * {@link #index} points at the bidder being asked. The controller opens as the {@link #highBidderId} with the opening
 * {@link #highBid}, or is asked for it first ({@link #awaitingOpeningBid}). A bid greater than {@code highBid} becomes
 * the high bid; once the next bidder would be the high bidder again, the high bid stands.
 */
public class LifeBidState {

    /** Whether a bidding is in progress (guards fresh initialization). */
    public boolean active;
    /** The bidding order, the controller first. */
    public final List<UUID> order = new ArrayList<>();
    /** Pointer into {@link #order} for the bidder being asked. */
    public int index;
    /** The current high bid. */
    public int highBid;
    /** The player holding the high bid. */
    public UUID highBidderId;
    /** The player being asked to bid. */
    public UUID currentBidderId;
    /** Whether the controller is being asked for the opening bid ("a bid of any number"). */
    public boolean awaitingOpeningBid;

    public void reset() {
        active = false;
        order.clear();
        index = 0;
        highBid = 0;
        highBidderId = null;
        currentBidderId = null;
        awaitingOpeningBid = false;
    }
}
