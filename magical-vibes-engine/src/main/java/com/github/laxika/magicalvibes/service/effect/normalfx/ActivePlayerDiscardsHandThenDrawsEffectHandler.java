package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerDiscardsHandThenDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
public class ActivePlayerDiscardsHandThenDrawsEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ActivePlayerDiscardsHandThenDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ActivePlayerDiscardsHandThenDrawsEffect) effect;
        UUID playerId = entry.getActivePlayerId() != null
                ? entry.getActivePlayerId() : entry.getControllerId();
        String playerName = gameData.playerIdToName.get(playerId);
        String cardName = entry.getCard().getName();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> discarded = hand == null ? List.of() : new ArrayList<>(hand);

        if (hand != null) {
            hand.clear();
        }
        gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());

        triggerCollectionService.beginDiscardEvent(gameData, playerId);
        for (Card card : discarded) {
            graveyardService.discardCard(gameData, playerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, playerId, card);
        }
        triggerCollectionService.finishDiscardEvent(gameData);

        if (discarded.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to discard (" + cardName + ")."));
        } else {
            String discardLog = playerName + " discards their hand (" + discarded.size()
                    + " card" + (discarded.size() != 1 ? "s" : "") + ") (" + cardName + ").";
            gameLogService.append(gameData, GameLog.text(discardLog));
            log.info("Game {} - {} discards hand of {} cards for {}",
                    gameData.id, playerName, discarded.size(), cardName);
        }

        for (int i = 0; i < e.drawAmount(); i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
        gameLogService.append(gameData, GameLog.text(playerName + " draws " + e.drawAmount()
                + " card" + (e.drawAmount() != 1 ? "s" : "") + "."));
    }
}
