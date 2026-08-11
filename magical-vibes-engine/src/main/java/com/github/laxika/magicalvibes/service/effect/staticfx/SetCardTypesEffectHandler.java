package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SetCardTypesEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetCardTypesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var set = (SetCardTypesEffect) effect;
        boolean matches = switch (set.scope()) {
            case ALL_PERMANENTS -> !context.source().getId().equals(context.target().getId())
                    && support.matchesStaticFilter(context, context.target(), null);
            case OWN_PERMANENTS -> context.targetOnSameBattlefield()
                    && support.matchesStaticFilter(context, context.target(), null);
            case SELF -> false;
            default -> support.matchesCreatureScope(context, set.scope(), null);
        };
        if (matches) {
            accumulator.setCardTypeOverriding(true);
            accumulator.setGrantedCardTypes(set.cardTypes());
        }
    }
}
