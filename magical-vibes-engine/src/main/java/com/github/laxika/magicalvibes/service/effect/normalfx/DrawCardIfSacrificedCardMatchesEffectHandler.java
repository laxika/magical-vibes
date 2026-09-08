package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfSacrificedCardMatchesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a draw rider based on the card sacrificed to pay the spell's additional cost. */
@Component
@RequiredArgsConstructor
public class DrawCardIfSacrificedCardMatchesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final DrawCardEffectHandler drawCardEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardIfSacrificedCardMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DrawCardIfSacrificedCardMatchesEffect draw = (DrawCardIfSacrificedCardMatchesEffect) effect;
        Card sacrificed = entry.getSacrificedCardSnapshot();
        if (sacrificed == null && entry.getSacrificedCardId() != null) {
            sacrificed = gameQueryService.findCardInGraveyardById(gameData, entry.getSacrificedCardId());
            if (sacrificed == null) {
                sacrificed = gameQueryService.findCardInExileById(gameData, entry.getSacrificedCardId());
            }
        }
        if (sacrificed == null
                || !predicateEvaluationService.matchesCardPredicate(sacrificed, draw.filter(), entry.getCard().getId())) {
            return;
        }
        drawCardEffectHandler.resolve(gameData, entry, new DrawCardEffect(draw.amount()));
    }
}
