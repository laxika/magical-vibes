package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.IllicitAuctionState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MagesContestEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the life auction and conditional counterspell of Mages' Contest. */
@Component
@RequiredArgsConstructor
public class MagesContestEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeBidAuctionSupport lifeBidAuctionSupport;
    private final LifeSupport lifeSupport;
    private final CounterSupport counterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MagesContestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        IllicitAuctionState state = gameData.illicitAuction;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            StackEntry target = findTargetSpell(gameData, entry.getTargetId());
            if (target == null) {
                return;
            }

            List<UUID> order = new ArrayList<>();
            order.add(entry.getControllerId());
            if (!entry.getControllerId().equals(target.getControllerId())) {
                order.add(target.getControllerId());
            }
            lifeBidAuctionSupport.begin(gameData, order, 1);
            promptNextBidderOrFinish(gameData, entry, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            int bid = gameData.chosenXValue;
            gameData.chosenXValue = null;
            UUID bidder = state.currentBidderId;
            String bidderName = gameData.playerIdToName.get(bidder);
            if (lifeBidAuctionSupport.recordBid(gameData, bid)) {
                gameLogService.append(gameData,
                        GameLog.text(bidderName + " bids " + bid + " life for " + cardName + "."));
            } else {
                gameLogService.append(gameData, GameLog.text(bidderName + " passes on " + cardName + "."));
            }
            promptNextBidderOrFinish(gameData, entry, cardName);
        }
    }

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
                new PendingInteraction.MagesContestBidChoice(
                        next,
                        state.highBid,
                        LifeBidAuctionSupport.MAX_BID,
                        cardName,
                        entry.getTargetId(),
                        state.highBidderId));
    }

    private void finish(GameData gameData, StackEntry entry, String cardName) {
        IllicitAuctionState state = gameData.illicitAuction;
        UUID winnerId = state.highBidderId;
        int amount = state.highBid;
        gameData.rerunCurrentEffectAfterInteraction = false;

        StackEntry target = findTargetSpell(gameData, entry.getTargetId());
        if (target != null) {
            lifeSupport.applyLifeLoss(gameData, winnerId, amount, cardName);
            StackEntry counterTarget = counterSupport.findCounterTarget(gameData, entry.getTargetId(), entry);
            if (counterTarget != null && winnerId.equals(entry.getControllerId())) {
                counterSupport.counterSpell(gameData, entry, counterTarget);
            }
        }
        state.reset();
    }

    private StackEntry findTargetSpell(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        return gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getCard().getId().equals(targetCardId))
                .findFirst()
                .orElse(null);
    }
}
