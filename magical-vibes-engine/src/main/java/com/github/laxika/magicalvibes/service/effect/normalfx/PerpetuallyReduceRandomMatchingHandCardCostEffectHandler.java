package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceRandomMatchingHandCardCostEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Records a perpetual generic cost reduction on one random matching hand card. */
@Component
@RequiredArgsConstructor
public class PerpetuallyReduceRandomMatchingHandCardCostEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyReduceRandomMatchingHandCardCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reduction = (PerpetuallyReduceRandomMatchingHandCardCostEffect) effect;
        List<Card> matchingCards = gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).stream()
                .filter(card -> reduction.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, reduction.filter(), null, gameData, entry.getControllerId()))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        Card selected = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        gameData.perpetualCardCastCostReductions.merge(
                selected.getId(), reduction.amount(), Integer::sum);
    }
}
