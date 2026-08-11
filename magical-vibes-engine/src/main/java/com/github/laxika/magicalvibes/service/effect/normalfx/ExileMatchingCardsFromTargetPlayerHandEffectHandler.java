package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMatchingCardsFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileMatchingCardsFromTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileMatchingCardsFromTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        var exileEffect = (ExileMatchingCardsFromTargetPlayerHandEffect) effect;
        List<Card> matchingCards = hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, exileEffect.filter(), null, gameData, targetPlayerId))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        hand.removeAll(matchingCards);
        matchingCards.forEach(card -> gameData.addToExile(targetPlayerId, card));
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + matchingCards.size()
                + " matching card" + (matchingCards.size() == 1 ? "" : "s") + " from their hand."));
    }
}
