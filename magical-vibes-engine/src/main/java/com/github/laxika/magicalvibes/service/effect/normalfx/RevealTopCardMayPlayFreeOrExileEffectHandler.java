package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardMayPlayFreeOrExileEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPlayFreeOrExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardMayPlayFreeOrExileEffect e = (RevealTopCardMayPlayFreeOrExileEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        Card topCard = deck.getFirst();

        String revealLog = playerName + " reveals " + topCard.getName() + " from the top of their library (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, revealLog);
        log.info("Game {} - {} reveals top card: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        // Lands can only be played if it's the controller's turn and they haven't played a land this turn
        if (topCard.hasType(CardType.LAND)) {
            boolean isControllersTurn = controllerId.equals(gameData.activePlayerId);
            int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(controllerId, 0);
            if (!isControllersTurn || landsPlayed >= gameData.getMaxLandsThisTurn(controllerId)) {
                String reason = !isControllersTurn ? "not controller's turn" : "land already played this turn";
                if (e.exileIfNotPlayed()) {
                    // Can't play the land — exile it
                    deck.removeFirst();
                    exileService.exileCard(gameData, controllerId, topCard);
                    gameBroadcastService.logAndBroadcast(gameData,
                            topCard.getName() + " can't be played (" + reason + ") and is exiled.");
                    log.info("Game {} - {} exiled (can't play land: {})", gameData.id, topCard.getName(), reason);
                } else {
                    // Can't play the land — it stays on top of the library
                    gameBroadcastService.logAndBroadcast(gameData,
                            topCard.getName() + " can't be played (" + reason + ") and stays on top of the library.");
                    log.info("Game {} - {} stays on top (can't play land: {})", gameData.id, topCard.getName(), reason);
                }
                return;
            }
        }

        // Card can be played — queue may ability
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(e),
                sourceName + " — Play " + topCard.getName() + " without paying its mana cost?"
        ));
    
    }
}
