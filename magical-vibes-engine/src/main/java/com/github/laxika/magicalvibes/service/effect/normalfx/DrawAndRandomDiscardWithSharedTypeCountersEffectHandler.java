package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawAndRandomDiscardWithSharedTypeCountersEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawAndRandomDiscardWithSharedTypeCountersEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawAndRandomDiscardWithSharedTypeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawAndRandomDiscardWithSharedTypeCountersEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        // Step 1: Draw cards
        playerInteractionSupport.applyDrawCards(gameData, controllerId, e.drawAmount());

        // Step 2: Discard cards at random, tracking what was discarded
        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Card> discardedCards = new ArrayList<>();
        gameData.discardCausedByOpponent = false;

        for (int i = 0; i < e.discardAmount(); i++) {
            if (hand == null || hand.isEmpty()) break;
            int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
            Card discarded = hand.remove(randomIndex);
            discardedCards.add(discarded);
            graveyardService.discardCard(gameData, controllerId, discarded);
            String logEntry = playerName + " discards " + discarded.getName() + " at random.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} discards {} at random ({})", gameData.id, playerName, discarded.getName(), sourceName);
            triggerCollectionService.checkDiscardTriggers(gameData, controllerId, discarded);
        }

        // Process any pending self-discard triggers
        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }

        // Step 3: Check if discarded cards share at least one card type
        if (discardedCards.size() >= 2 && playerInteractionSupport.sharesCardType(discardedCards)) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId != null) {
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source != null && !gameQueryService.cantHaveCounters(gameData, source)) {
                    source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + e.counterAmount());
                    String logEntry = sourceName + " gets " + e.counterAmount()
                            + " +1/+1 counter" + (e.counterAmount() != 1 ? "s" : "")
                            + " (discarded cards share a card type).";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} gets {} +1/+1 counters (shared card type)", gameData.id, sourceName, e.counterAmount());
                }
            }
        } else if (discardedCards.size() >= 2) {
            String logEntry = sourceName + "'s discarded cards do not share a card type — no counters.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} discarded cards do not share a card type", gameData.id, sourceName);
        }
    
    }
}
