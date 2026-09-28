package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;
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

/**
 * Resolves {@link TargetPlayerDiscardsHandThenDrawsThatManyEffect}: the targeted player discards
 * their entire hand, then draws that many cards minus the effect's optional fixed reduction.
 * Discards are automatic; the draw count is floored at zero. Mirrors
 * {@link DiscardOwnHandThenDrawThatManyEffectHandler} for a target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsHandThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
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
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        List<Card> discarded = new ArrayList<>(hand);
        int discardCount = discarded.size();
        hand.clear();
        gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());

        triggerCollectionService.beginDiscardEvent(gameData, playerId);
        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }
        triggerCollectionService.finishDiscardEvent(gameData);

        String discardLog = playerName + " discards their hand (" + discardCount
                + " card" + (discardCount != 1 ? "s" : "") + ") (" + cardName + ").";
        gameLogService.append(gameData, GameLog.text(discardLog));
        log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discardCount, cardName);

        int drawReduction = ((TargetPlayerDiscardsHandThenDrawsThatManyEffect) effect).drawReduction();
        int drawCount = Math.max(0, discardCount - drawReduction);
        for (int i = 0; i < drawCount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
        String drawLog = playerName + " draws " + drawCount + " card" + (drawCount != 1 ? "s" : "") + ".";
        gameLogService.append(gameData, GameLog.text(drawLog));
        log.info("Game {} - {} draws {} cards for {}", gameData.id, playerName, drawCount, cardName);
    }
}
