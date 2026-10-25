package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.IllicitAuctionState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PainsRewardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveRiderForPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Pain's Reward's life auction and reward. */
@Component
@RequiredArgsConstructor
public class PainsRewardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeBidAuctionSupport lifeBidAuctionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PainsRewardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        IllicitAuctionState state = gameData.illicitAuction;
        String cardName = entry.getCard().getName();

        if (!state.active) {
            List<UUID> order = new ArrayList<>();
            order.add(entry.getControllerId());
            for (UUID playerId : gameData.orderedPlayerIds) {
                if (!playerId.equals(entry.getControllerId())) {
                    order.add(playerId);
                }
            }
            lifeBidAuctionSupport.begin(gameData, order, 0);
            state.currentBidderId = entry.getControllerId();
            prompt(gameData, state, cardName, true);
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }

        int bid = gameData.chosenXValue;
        gameData.chosenXValue = null;
        UUID bidder = state.currentBidderId;
        String bidderName = gameData.playerIdToName.get(bidder);
        if (lifeBidAuctionSupport.recordBid(gameData, bid)) {
            gameLogService.append(gameData,
                    GameLog.text(bidderName + " bids " + bid + " life for " + cardName + "."));
        } else {
            gameLogService.append(gameData,
                    GameLog.text(bidderName + " passes on " + cardName + "."));
        }

        UUID next = lifeBidAuctionSupport.advanceToNextBidder(gameData);
        if (next == null) {
            finish(gameData, entry, state);
        } else {
            state.currentBidderId = next;
            prompt(gameData, state, cardName, false);
        }
    }

    private void prompt(GameData gameData, IllicitAuctionState state, String cardName,
                        boolean openingBid) {
        gameData.rerunCurrentEffectAfterInteraction = true;
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PainsRewardBidChoice(
                        state.currentBidderId,
                        state.highBid,
                        LifeBidAuctionSupport.MAX_BID,
                        cardName,
                        state.highBidderId,
                        openingBid));
    }

    /**
     * The high bidder loses the high bid and then draws four cards (CR 608.2c), as steps of their own after the
     * auction, so a Lich's Duel Mastery sacrifice replacing the loss asks its CR 616.1 questions without redoing
     * the bidding.
     */
    private void finish(GameData gameData, StackEntry entry, IllicitAuctionState state) {
        UUID winnerId = state.highBidderId;
        int amount = state.highBid;
        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
        if (winnerId == null) {
            return;
        }

        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                new PlayersLoseLifeEffect(Map.of(winnerId, amount)),
                new ResolveRiderForPlayerEffect(new DrawCardEffect(4), winnerId, null, 0, null, null)));
    }
}
