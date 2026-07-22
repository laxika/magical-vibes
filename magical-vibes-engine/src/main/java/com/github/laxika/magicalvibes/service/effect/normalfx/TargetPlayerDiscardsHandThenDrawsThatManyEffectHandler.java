package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TargetPlayerDiscardsHandThenDrawsThatManyEffect}: the targeted player discards
 * their entire hand, then draws that many cards. Discards are automatic; draw count equals the
 * number discarded. Mirrors {@link DiscardOwnHandThenDrawThatManyEffectHandler} for a target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsHandThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsHandThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId();
        if (playerId == null) {
            return;
        }

        String playerName = gameData.playerIdToName.get(playerId);
        String cardName = entry.getCard().getName();
        List<Card> hand = gameData.playerHands.get(playerId);

        if (hand == null || hand.isEmpty()) {
            String logEntry = playerName + " has no cards to discard (" + cardName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        List<Card> discarded = new ArrayList<>(hand);
        int discardCount = discarded.size();
        hand.clear();
        gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());

        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }

        String discardLog = playerName + " discards their hand (" + discardCount
                + " card" + (discardCount != 1 ? "s" : "") + ") (" + cardName + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(discardLog));
        log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discardCount, cardName);

        for (int i = 0; i < discardCount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
        String drawLog = playerName + " draws " + discardCount + " card" + (discardCount != 1 ? "s" : "") + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(drawLog));
        log.info("Game {} - {} draws {} cards for {}", gameData.id, playerName, discardCount, cardName);
    }
}
