package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardAndGainLifeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final LifeSupport lifeSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        ExileTargetCardFromGraveyardAndGainLifeEffect effect =
                (ExileTargetCardFromGraveyardAndGainLifeEffect) effectToResolve;
        UUID targetCardId = entry.getTargetCardIds() == null || entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId()
                : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, targetCardId);

        if (targetCard == null || effect.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(targetCard, effect.filter(), null)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer a valid target)."));
            return;
        }

        graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard);
        gameLogService.append(gameData,
                GameLog.textCardText(gameData.playerIdToName.get(entry.getControllerId()) + " exiles ",
                        targetCard, " from a graveyard."));
        if (effect.lifeGain() > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), effect.lifeGain(),
                    entry.getCard().getName(), entry.getCard(), entry.getEntryType());
        }
    }
}
