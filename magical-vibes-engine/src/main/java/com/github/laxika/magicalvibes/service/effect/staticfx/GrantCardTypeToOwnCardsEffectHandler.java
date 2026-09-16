package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeToOwnCardsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GrantCardTypeToOwnCardsEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantCardTypeToOwnCardsEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var grant = (GrantCardTypeToOwnCardsEffect) effect;
        if (context.target().getCard().isToken()
                || !Objects.equals(context.sourceControllerId(), context.target().getCard().getOwnerId())
                || !support.matchesCardFilter(context, context.target().getCard(), grant.filter())) {
            return;
        }
        accumulator.addGrantedCardType(grant.cardType());
    }
}
