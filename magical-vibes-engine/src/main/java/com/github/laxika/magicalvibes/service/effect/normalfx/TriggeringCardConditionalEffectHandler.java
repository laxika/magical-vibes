package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TriggeringCardConditionalEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TriggeringCardConditionalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TriggeringCardConditionalEffect conditional = (TriggeringCardConditionalEffect) effect;
        Card triggeringCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringCard == null || !predicateEvaluationService.matchesCardPredicate(
                triggeringCard, conditional.predicate(), entry.getCard().getId(), gameData,
                entry.getControllerId(), entry.getSourcePermanentId(), null,
                entry.getXValue(), entry.getSourcePermanentSnapshot())) {
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(conditional.wrapped());
        if (handler != null) {
            handler.resolve(gameData, entry, conditional.wrapped());
        } else {
            log.warn("No handler for conditional effect: {}", conditional.wrapped().getClass().getSimpleName());
        }
    }
}
