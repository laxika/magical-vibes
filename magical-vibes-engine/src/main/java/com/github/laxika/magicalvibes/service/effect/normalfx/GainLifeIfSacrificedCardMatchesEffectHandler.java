package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeIfSacrificedCardMatchesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a life-gain rider based on the card sacrificed to pay the ability's cost. */
@Component
@RequiredArgsConstructor
public class GainLifeIfSacrificedCardMatchesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GainLifeEffectHandler gainLifeEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainLifeIfSacrificedCardMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GainLifeIfSacrificedCardMatchesEffect gain = (GainLifeIfSacrificedCardMatchesEffect) effect;
        Card sacrificed = entry.getSacrificedCardSnapshot();
        if (sacrificed == null && entry.getSacrificedPermanentSnapshot() != null) {
            sacrificed = entry.getSacrificedPermanentSnapshot().getCard();
        }
        if (sacrificed == null && entry.getSacrificedCardId() != null) {
            sacrificed = gameQueryService.findCardInGraveyardById(gameData, entry.getSacrificedCardId());
            if (sacrificed == null) {
                sacrificed = gameQueryService.findCardInExileById(gameData, entry.getSacrificedCardId());
            }
        }
        if (sacrificed == null
                || !predicateEvaluationService.matchesCardPredicate(sacrificed, gain.filter(), entry.getCard().getId())) {
            return;
        }
        gainLifeEffectHandler.resolve(gameData, entry, new GainLifeEffect(gain.amount()));
    }
}
