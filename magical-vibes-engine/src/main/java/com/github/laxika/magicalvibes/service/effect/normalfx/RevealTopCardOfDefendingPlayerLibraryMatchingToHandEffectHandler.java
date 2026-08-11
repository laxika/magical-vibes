package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffect revealEffect =
                (RevealTopCardOfDefendingPlayerLibraryMatchingToHandEffect) effect;
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return;
        }

        UUID defendingPlayerId = gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (defendingPlayerId == null) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(defendingPlayerId);
        String playerName = gameData.playerIdToName.get(defendingPlayerId);
        String sourceName = entry.getCard().getName();
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard)
                .text(" from the top of their library (" + sourceName + ").").build());

        if (!predicateEvaluationService.matchesCardPredicate(
                topCard, revealEffect.matchPredicate(), null, gameData, defendingPlayerId)) {
            return;
        }

        deck.removeFirst();
        gameData.addCardToHand(defendingPlayerId, topCard);
        gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(topCard)
                .text(" into their hand (" + sourceName + ").").build());
    }
}
