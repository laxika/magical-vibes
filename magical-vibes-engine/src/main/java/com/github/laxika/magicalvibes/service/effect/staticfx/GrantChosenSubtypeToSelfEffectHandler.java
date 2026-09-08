package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Self-only companion to {@link GrantChosenSubtypeToOwnCreaturesEffectHandler}: the source permanent
 * is never a target of the "others" handler, so this grants the chosen subtype to the source itself
 * when the effect uses {@link GrantScope#SELF} (Adaptive Automaton).
 */
@Component
@RequiredArgsConstructor
public class GrantChosenSubtypeToSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenSubtypeToOwnCreaturesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantChosenSubtypeToOwnCreaturesEffect) effect;
        if (grant.scope() != GrantScope.SELF) return;
        CardSubtype chosenSubtype = context.source().getChosenSubtype();
        if (chosenSubtype == null) return;
        if (!support.matchesStaticFilter(context, context.source(), grant.filter())) return;
        accumulator.addGrantedSubtype(chosenSubtype);
    }
}
