package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetLegendaryCreatureCardFromGraveyardWithMemoryCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetLegendaryCreatureCardFromGraveyardWithMemoryCounterEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetLegendaryCreatureCardFromGraveyardWithMemoryCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetCardIds().isEmpty() && entry.getTargetId() == null) {
            return;
        }

        var targetCardId = entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId() : entry.getTargetCardIds().getFirst();
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || !predicateEvaluationService.matchesCardPredicate(
                targetCard, ExileTargetLegendaryCreatureCardFromGraveyardWithMemoryCounterEffect.targetFilter(),
                entry.getCard().getId())) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " does nothing because its target is no longer valid."));
            return;
        }

        if (graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            gameData.exiledCardsWithMemoryCounters.add(targetCardId);
            gameLogService.append(gameData,
                    GameLog.textCardText(entry.getCard().getName() + " exiles ", targetCard,
                            " with a memory counter on it."));
        }
    }
}
