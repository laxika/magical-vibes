package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardMatchingToHandEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardMatchingToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardMatchingToHandEffect e = (RevealTopCardMatchingToHandEffect) effect;

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

        // A non-matching card is left revealed on top of the library.
        if (!predicateEvaluationService.matchesCardPredicate(topCard, e.matchPredicate(), null, gameData, controllerId)) {
            return;
        }

        deck.removeFirst();
        gameData.addCardToHand(controllerId, topCard);
        gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(topCard)
                .text(" into their hand (" + sourceName + ").").build());

        log.info("Game {} - {} puts revealed {} into hand via {}", gameData.id, playerName, topCard.getName(), sourceName);
    }
}
