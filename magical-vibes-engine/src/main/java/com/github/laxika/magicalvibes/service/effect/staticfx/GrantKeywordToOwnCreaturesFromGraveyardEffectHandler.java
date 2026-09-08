package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnCreaturesFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCardInGraveyard;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantKeywordToOwnCreaturesFromGraveyardEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final ConditionEvaluationService conditionEvaluationService;

    private static final SourceCardInGraveyard SOURCE_IN_GRAVEYARD = new SourceCardInGraveyard();

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToOwnCreaturesFromGraveyardEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantKeywordToOwnCreaturesFromGraveyardEffect) effect;
        ConditionContext conditionContext = ConditionContext.forStaticEffect(
                context.source(), context.sourceControllerId());
        if (!conditionEvaluationService.isMet(context.gameData(), SOURCE_IN_GRAVEYARD, conditionContext)
                || !conditionEvaluationService.isMet(context.gameData(), grant.condition(), conditionContext)) {
            return;
        }
        if (support.matchesCreatureScope(context, grant.scope(), grant.filter())) {
            accumulator.addKeywords(grant.keywords());
        }
    }
}
