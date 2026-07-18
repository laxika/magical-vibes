package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.IllicitAuctionState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.IllicitAuctionEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link IllicitAuctionEffect} (Illicit Auction): each player may bid life for control of
 * the target creature. The controller opens the bidding at 0; then, in turn order, each player may
 * top the high bid. The auction ends once the high bid stands (comes back around to the high bidder
 * with no raise). The high bidder loses life equal to the high bid — a life loss, so a player may bid
 * more life than they have — and gains control of the creature indefinitely.
 *
 * <p>The flow is driven one bidder at a time and re-runs on every bid answer (kept alive via
 * {@link GameData#rerunCurrentEffectAfterInteraction}, since the bid is not an X-value choice). Each
 * prompt is an {@link PendingInteraction.IllicitAuctionBidChoice}; the answer arrives on
 * {@link GameData#chosenXValue}. Progress lives on {@link GameData#illicitAuction}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IllicitAuctionEffectHandler implements NormalEffectHandlerBean {

    /** Generous cap on a single bid; a life loss can exceed the bidder's life total (per ruling). */
    private static final int MAX_BID = 999;

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IllicitAuctionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        IllicitAuctionState state = gameData.illicitAuction;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            // Fresh entry: the target must still be on the battlefield to auction.
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null) {
                return;
            }
            state.reset();
            state.active = true;
            UUID controllerId = entry.getControllerId();
            state.order.add(controllerId);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(controllerId)) {
                    state.order.add(playerId);
                }
            }
            state.highBid = 0;
            state.highBidderId = controllerId;
            state.index = 0; // points at the controller, who opens the bidding at 0
            promptNextBidderOrFinish(gameData, entry, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            int bid = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID bidder = state.currentBidderId;
            String bidderName = gameData.playerIdToName.get(bidder);

            if (bid > state.highBid) {
                state.highBid = bid;
                state.highBidderId = bidder;
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(bidderName + " bids " + bid + " life for " + cardName + "."));
            } else {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(bidderName + " passes on " + cardName + "."));
            }
            promptNextBidderOrFinish(gameData, entry, cardName);
        }
    }

    /**
     * Advances to the next bidder in turn order. If the bid has come all the way back around to the
     * high bidder, the high bid stands and the auction finishes; otherwise prompts the next bidder.
     */
    private void promptNextBidderOrFinish(GameData gameData, StackEntry entry, String cardName) {
        IllicitAuctionState state = gameData.illicitAuction;
        state.index = (state.index + 1) % state.order.size();
        UUID next = state.order.get(state.index);
        if (next.equals(state.highBidderId)) {
            finish(gameData, entry, cardName);
            return;
        }
        state.currentBidderId = next;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        String targetName = target != null ? target.getCard().getName() : "the creature";
        String highBidderName = gameData.playerIdToName.get(state.highBidderId);
        String prompt = "Bid life for control of " + targetName + " (current high bid: " + state.highBid
                + " by " + highBidderName + "). Enter more than " + state.highBid + " to bid, or "
                + state.highBid + " or less to pass.";

        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.IllicitAuctionBidChoice(next, state.highBid, MAX_BID, cardName, prompt));
    }

    /**
     * The high bid stood: the high bidder loses that much life and gains control of the creature
     * indefinitely. A bid of 0 (everyone passed) simply hands control to the controller for free.
     */
    private void finish(GameData gameData, StackEntry entry, String cardName) {
        IllicitAuctionState state = gameData.illicitAuction;
        UUID winnerId = state.highBidderId;
        int amount = state.highBid;
        gameData.rerunCurrentEffectAfterInteraction = false;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target != null) {
            if (amount > 0) {
                lifeSupport.applyLifeLoss(gameData, winnerId, amount, cardName);
            }
            creatureControlService.applyControlEffect(gameData, winnerId, target,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    ControlDuration.PERMANENT.toEffectDuration(), null, cardName);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(gameData.playerIdToName.get(winnerId) + " gains control of ", target.getCard(), "."));
        }
        state.reset();
    }
}
