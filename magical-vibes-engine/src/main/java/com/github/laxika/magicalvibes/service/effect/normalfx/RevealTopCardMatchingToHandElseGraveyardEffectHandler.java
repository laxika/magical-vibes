package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardMatchingToHandElseGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMatchingToHandElseGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardMatchingToHandElseGraveyardEffect revealEffect =
                (RevealTopCardMatchingToHandElseGraveyardEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library (" + sourceName + ").").build());

        deck.removeFirst();
        if (predicateEvaluationService.matchesCardPredicate(
                topCard, revealEffect.matchPredicate(), null, gameData, controllerId)) {
            gameData.addCardToHand(controllerId, topCard);
            gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(topCard)
                    .text(" into their hand (" + sourceName + ").").build());
        } else {
            gameData.playerGraveyards.get(controllerId).add(topCard);
            gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(topCard)
                    .text(" into their graveyard (" + sourceName + ").").build());
        }

        log.info("Game {} - {} moves revealed {} via {}", gameData.id, playerName, topCard.getName(), sourceName);
    }
}
