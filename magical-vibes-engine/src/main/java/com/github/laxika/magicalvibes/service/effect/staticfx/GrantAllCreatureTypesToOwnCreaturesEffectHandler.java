package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

@Component
public class GrantAllCreatureTypesToOwnCreaturesEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    public GrantAllCreatureTypesToOwnCreaturesEffectHandler(StaticEffectSupport support) {
        this.support = support;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAllCreatureTypesToOwnCreaturesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (!support.matchesCreatureScope(context, GrantScope.OWN_CREATURES, null)) return;
        for (CardSubtype subtype : CardSubtype.values()) {
            if (StaticEffectSupport.isCreatureSubtype(subtype)) {
                accumulator.addGrantedSubtype(subtype);
            }
        }
    }
}
