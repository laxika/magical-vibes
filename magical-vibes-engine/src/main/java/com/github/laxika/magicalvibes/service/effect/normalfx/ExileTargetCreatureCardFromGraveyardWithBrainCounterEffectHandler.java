package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardWithBrainCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Rex's targeted graveyard exile and brain-counter placement. */
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureCardFromGraveyardWithBrainCounterEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureCardFromGraveyardWithBrainCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetCardIds() == null || entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId() : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null || !predicateEvaluationService.matchesCardPredicate(
                targetCard, ExileTargetCreatureCardFromGraveyardWithBrainCounterEffect.targetFilter(),
                entry.getCard().getId())) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer a creature card in a graveyard)."));
            return;
        }

        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        gameData.exiledCardsWithBrainCounters.add(targetCardId);
        gameLogService.append(gameData,
                GameLog.textCardText("Exiled ", targetCard, " with a brain counter on it."));
    }
}
