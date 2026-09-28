package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetChosenNameAndCreatureTypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetChosenNameAndCreatureTypeEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetChosenNameAndCreatureTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        SetChosenNameAndCreatureTypeEffect set = (SetChosenNameAndCreatureTypeEffect) effect;
        boolean applies = set.scope() == GrantScope.SELF
                ? context.target().getId().equals(context.sourceId())
                : support.matchesCreatureScope(context, set.scope(), null);
        if (!applies) return;

        String chosenName = context.source().getChosenName();
        if (chosenName != null) {
            accumulator.setName(chosenName);
        }
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype != null) {
            accumulator.addGrantedSubtype(chosenSubtype);
            accumulator.setSubtypeOverriding(true);
        }
    }
}
