package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardMayPlayFreeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMayPlayFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardMayPlayFreeEffect e = (RevealTopCardMayPlayFreeEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameLogService.append(gameData, GameLog.text(logEntry));
            return;
        }

        Card topCard = deck.getFirst();

        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard).text(" from the top of their library (" + sourceName + ").").build());
        log.info("Game {} - {} reveals top card: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        // Descendants' Path: only a creature card sharing a creature type with a creature you
        // control may be cast; anything else never gets offered.
        if (e.requireCreatureSharingTypeWithYourCreatures()
                && !(topCard.hasType(CardType.CREATURE)
                        && gameQueryService.cardSharesCreatureTypeWithControlledCreature(gameData, topCard, controllerId))) {
            disposeOfUnplayedCard(gameData, controllerId, deck, topCard, e.notPlayedDestination(),
                    "doesn't share a creature type with a creature you control");
            return;
        }

        // Lands can only be played if it's the controller's turn and they haven't played a land this turn
        if (topCard.hasType(CardType.LAND)) {
            boolean isControllersTurn = controllerId.equals(gameData.activePlayerId);
            int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(controllerId, 0);
            if (!isControllersTurn || landsPlayed >= gameData.getMaxLandsThisTurn(controllerId)) {
                String reason = !isControllersTurn ? "not controller's turn" : "land already played this turn";
                disposeOfUnplayedCard(gameData, controllerId, deck, topCard, e.notPlayedDestination(),
                        "can't be played (" + reason + ")");
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

    private void disposeOfUnplayedCard(GameData gameData, UUID controllerId, List<Card> deck, Card topCard, LookDestination destination, String reason) {
        switch (destination) {
            case EXILE -> {
                deck.removeFirst();
                exileService.exileCard(gameData, controllerId, topCard);
                gameLogService.append(gameData, GameLog.builder().card(topCard).text(" " + reason + " and is exiled.").build());
                log.info("Game {} - {} exiled ({})", gameData.id, topCard.getName(), reason);
            }
            case BOTTOM_OF_LIBRARY -> {
                deck.removeFirst();
                deck.add(topCard);
                gameLogService.append(gameData, GameLog.builder().card(topCard).text(" " + reason + " and is put on the bottom of the library.").build());
                log.info("Game {} - {} bottomed ({})", gameData.id, topCard.getName(), reason);
            }
            case HAND -> {
                deck.removeFirst();
                gameData.playerHands.get(controllerId).add(topCard);
                gameLogService.append(gameData, GameLog.builder().card(topCard).text(" " + reason + " and is put into the player's hand.").build());
                log.info("Game {} - {} put into hand ({})", gameData.id, topCard.getName(), reason);
            }
            default -> {
                gameLogService.append(gameData, GameLog.builder().card(topCard).text(" " + reason + " and stays on top of the library.").build());
                log.info("Game {} - {} stays on top ({})", gameData.id, topCard.getName(), reason);
            }
        }
    }
}
