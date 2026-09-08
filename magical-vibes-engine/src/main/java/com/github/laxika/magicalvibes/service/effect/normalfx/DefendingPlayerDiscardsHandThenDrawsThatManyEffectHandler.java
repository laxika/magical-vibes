package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DefendingPlayerDiscardsHandThenDrawsThatManyEffect}. The attacked player, or the
 * controller of an attacked planeswalker, discards automatically and draws the same number.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefendingPlayerDiscardsHandThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DefendingPlayerDiscardsHandThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackedTargetId = entry.getAttackedTargetId();
        UUID playerId = defendingPlayerId(gameData, attackedTargetId);
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

        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }

        gameLogService.append(gameData, GameLog.text(playerName + " discards their hand (" + discardCount
                + " card" + (discardCount != 1 ? "s" : "") + ") (" + cardName + ")."));
        log.info("Game {} - {} discards hand of {} cards for {}", gameData.id, playerName, discardCount, cardName);

        for (int i = 0; i < discardCount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + " draws " + discardCount
                + " card" + (discardCount != 1 ? "s" : "") + "."));
        log.info("Game {} - {} draws {} cards for {}", gameData.id, playerName, discardCount, cardName);
    }

    private UUID defendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        if (gameData.playerIds.contains(attackedTargetId)) {
            return attackedTargetId;
        }
        Permanent attackedPermanent = gameQueryService.findPermanentById(gameData, attackedTargetId);
        return attackedPermanent == null ? null
                : gameQueryService.findPermanentController(gameData, attackedPermanent.getId());
    }
}
