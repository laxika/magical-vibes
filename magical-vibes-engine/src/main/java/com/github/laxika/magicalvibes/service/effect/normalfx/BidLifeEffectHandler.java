package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LifeBidState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.BidLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the life-bid step {@link BidLifeEffect}. The controller opens the bidding (at the opening bid, or asked for
 * "a bid of any number"); then each other bidder, in turn order from the controller (CR 101.4), is asked in a
 * {@link PendingInteraction.LifeBidChoice} to top the high bid or pass, round after round, until the bidding comes back
 * to the high bidder with no raise. A lone bidder's opening bid stands at once, unasked.
 *
 * <p>The handler resolves again on each answer ({@link GameData#rerunCurrentEffectAfterInteraction}) and changes nothing
 * but its own progress ({@link GameData#lifeBid}) and the bid log lines, so a later step may read it: once the high bid
 * stands it records {@link StepKind#BID} (the high bid, the high bidder). The life loss and the prize are the steps
 * that follow, so a Lich's Duel Mastery sacrifice replacing that loss asks its CR 616.1 questions in the loss's own step
 * without redoing the bidding.
 */
@Component
@RequiredArgsConstructor
public class BidLifeEffectHandler implements NormalEffectHandlerBean {

    /** A bid is a life loss, not a payment: any number may be bid (rulings). */
    static final int MAX_BID = Integer.MAX_VALUE;

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BidLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var bid = (BidLifeEffect) effect;
        LifeBidState state = gameData.lifeBid;
        String cardName = FaceDownVisibility.sourceName(gameData, entry);

        if (!state.active) {
            state.reset();
            state.active = true;
            state.order.addAll(biddingOrder(gameData, entry, bid));
            state.highBidderId = state.order.getFirst();
            state.highBid = bid.openingBid() == null ? 0 : bid.openingBid();
            state.index = 0;
            if (bid.openingBid() == null) {
                state.awaitingOpeningBid = true;
                state.currentBidderId = state.highBidderId;
                prompt(gameData, entry, bid, cardName);
            } else {
                promptNextBidderOrRecord(gameData, entry, bid, cardName);
            }
            return;
        }

        if (gameData.chosenXValue == null) {
            return;
        }
        int amount = gameData.chosenXValue;
        gameData.chosenXValue = null;
        String bidderName = gameData.playerIdToName.get(state.currentBidderId);
        if (state.awaitingOpeningBid || amount > state.highBid) {
            state.awaitingOpeningBid = false;
            state.highBid = amount;
            state.highBidderId = state.currentBidderId;
            gameLogService.append(gameData, GameLog.text(bidderName + " bids " + amount + " life for " + cardName + "."));
        } else {
            gameLogService.append(gameData, GameLog.text(bidderName + " passes on " + cardName + "."));
        }
        promptNextBidderOrRecord(gameData, entry, bid, cardName);
    }

    /** The controller, then the other bidders in turn order after the controller. */
    private List<UUID> biddingOrder(GameData gameData, StackEntry entry, BidLifeEffect bid) {
        UUID controllerId = entry.getControllerId();
        Set<UUID> bidders = new HashSet<>(subjectResolutionService.players(gameData, entry, bid, bid.bidders()));
        List<UUID> seats = gameData.orderedPlayerIds;
        int start = Math.max(0, seats.indexOf(controllerId));
        List<UUID> order = new ArrayList<>();
        order.add(controllerId);
        for (int i = 1; i < seats.size(); i++) {
            UUID playerId = seats.get((start + i) % seats.size());
            if (bidders.contains(playerId)) {
                order.add(playerId);
            }
        }
        return order;
    }

    /** Asks the next bidder, or records the step when the bidding is back at the high bidder (the high bid stands). */
    private void promptNextBidderOrRecord(GameData gameData, StackEntry entry, BidLifeEffect bid, String cardName) {
        LifeBidState state = gameData.lifeBid;
        state.index = (state.index + 1) % state.order.size();
        UUID next = state.order.get(state.index);
        if (next.equals(state.highBidderId)) {
            gameData.rerunCurrentEffectAfterInteraction = false;
            entry.recordStep(StepKind.BID, StepOutcome.ofAmount(state.highBid, List.of(state.highBidderId)));
            state.reset();
            return;
        }
        state.currentBidderId = next;
        prompt(gameData, entry, bid, cardName);
    }

    private void prompt(GameData gameData, StackEntry entry, BidLifeEffect bid, String cardName) {
        LifeBidState state = gameData.lifeBid;
        gameData.rerunCurrentEffectAfterInteraction = true;
        UUID prizeTargetId = bid.prize() == BidLifeEffect.Prize.CARDS ? null : entry.getTargetId();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LifeBidChoice(state.currentBidderId,
                state.highBid, MAX_BID, cardName, state.highBidderId, state.awaitingOpeningBid, bid.prize(),
                prizeTargetId));
    }
}
