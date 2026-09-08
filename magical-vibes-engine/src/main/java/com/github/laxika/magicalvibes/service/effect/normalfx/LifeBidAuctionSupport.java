package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.IllicitAuctionState;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Shared state transitions for effects that resolve through a life-bid auction. */
@Component
public class LifeBidAuctionSupport {

    public static final int MAX_BID = Integer.MAX_VALUE;

    public void begin(GameData gameData, List<UUID> order, int openingBid) {
        IllicitAuctionState state = gameData.illicitAuction;
        state.reset();
        state.active = true;
        state.order.addAll(order);
        state.highBid = openingBid;
        state.highBidderId = order.getFirst();
        state.index = 0;
    }

    public boolean recordBid(GameData gameData, int bid) {
        IllicitAuctionState state = gameData.illicitAuction;
        if (bid <= state.highBid) {
            return false;
        }
        state.highBid = bid;
        state.highBidderId = state.currentBidderId;
        return true;
    }

    public UUID advanceToNextBidder(GameData gameData) {
        IllicitAuctionState state = gameData.illicitAuction;
        state.index = (state.index + 1) % state.order.size();
        UUID next = state.order.get(state.index);
        return next.equals(state.highBidderId) ? null : next;
    }
}
