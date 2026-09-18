package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForEachChosenColorEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForEachChosenColorEffectHandler implements CostModificationHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForEachChosenColorEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (!source.controlledBy(context.castingPlayerId()) || source.sourcePermanent() == null) {
            return 0;
        }
        long matchingColors = source.sourcePermanent().getChosenColors().stream()
                .filter(gameQueryService.getEffectiveCardColors(context.gameData(), context.spell())::contains)
                .count();
        return -(int) matchingColors;
    }
}
