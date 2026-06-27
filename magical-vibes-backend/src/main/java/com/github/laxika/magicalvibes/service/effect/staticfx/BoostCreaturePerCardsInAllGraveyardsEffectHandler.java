package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostCreaturePerCardsInAllGraveyardsEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturePerCardsInAllGraveyardsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerCardsInAllGraveyardsEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        int count = support.countCardsInAllGraveyards(context.gameData(), boost.filter());
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }
}
