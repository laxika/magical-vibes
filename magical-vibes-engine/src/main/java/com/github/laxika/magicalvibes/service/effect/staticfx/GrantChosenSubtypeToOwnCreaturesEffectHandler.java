package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantChosenSubtypeToOwnCreaturesEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenSubtypeToOwnCreaturesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantChosenSubtypeToOwnCreaturesEffect) effect;
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        if (grant.scope() == GrantScope.SELF) return;
        if (support.matchesCreatureScope(context, grant.scope(), null)) {
            accumulator.addGrantedSubtype(chosenSubtype);
        }
    }
}
