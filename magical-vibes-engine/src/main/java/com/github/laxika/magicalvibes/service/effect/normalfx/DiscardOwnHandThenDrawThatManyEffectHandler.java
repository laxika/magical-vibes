package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardOwnHandThenDrawThatManyEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardOwnHandThenDrawThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Card> hand = gameData.playerHands.get(controllerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards to discard (" + cardName + ").";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        List<Card> discarded = new ArrayList<>(hand);
        int discardCount = discarded.size();
        hand.clear();
        gameData.discardCausedByOpponent = false;

        triggerCollectionService.beginDiscardEvent(gameData, controllerId);
        for (Card card : discarded) {
            graveyardService.discardCard(gameData, controllerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, controllerId, card);
        }
        triggerCollectionService.finishDiscardEvent(gameData);

        String discardLog = playerName + " discards their hand (" + discardCount
                + " card" + (discardCount != 1 ? "s" : "") + ") (" + cardName + ").";
        gameLogService.append(gameData, GameLog.text(discardLog));
        log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discardCount, cardName);

        for (int i = 0; i < discardCount; i++) {
            drawService.resolveDrawCard(gameData, controllerId);
        }
        String drawLog = playerName + " draws " + discardCount + " card" + (discardCount != 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(drawLog));
        log.info("Game {} - {} draws {} cards for {}", gameData.id, playerName, discardCount, cardName);
    
    }
}
