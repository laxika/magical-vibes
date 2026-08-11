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
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
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

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;
    private final LifeSupport lifeSupport;
    private final LifeBidAuctionSupport lifeBidAuctionSupport;

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
            UUID controllerId = entry.getControllerId();
            List<UUID> order = new ArrayList<>();
            order.add(controllerId);
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(controllerId)) {
                    order.add(playerId);
                }
            }
            lifeBidAuctionSupport.begin(gameData, order, 0);
            promptNextBidderOrFinish(gameData, entry, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            int bid = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID bidder = state.currentBidderId;
            String bidderName = gameData.playerIdToName.get(bidder);

            if (lifeBidAuctionSupport.recordBid(gameData, bid)) {
                gameLogService.append(gameData, GameLog.text(bidderName + " bids " + bid + " life for " + cardName + "."));
            } else {
                gameLogService.append(gameData, GameLog.text(bidderName + " passes on " + cardName + "."));
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
        UUID next = lifeBidAuctionSupport.advanceToNextBidder(gameData);
        if (next == null) {
            finish(gameData, entry, cardName);
            return;
        }
        state.currentBidderId = next;

        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.IllicitAuctionBidChoice(
                        next,
                        state.highBid,
                        LifeBidAuctionSupport.MAX_BID,
                        cardName,
                        entry.getTargetId(),
                        state.highBidderId));
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
            gameLogService.append(gameData, GameLog.textCardText(gameData.playerIdToName.get(winnerId) + " gains control of ", target.getCard(), "."));
        }
        state.reset();
    }
}
