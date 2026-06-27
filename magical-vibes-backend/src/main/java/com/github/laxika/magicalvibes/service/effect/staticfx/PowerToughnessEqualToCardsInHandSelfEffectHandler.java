package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PowerToughnessEqualToCardsInHandSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PowerToughnessEqualToCardsInHandEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> hand = context.gameData().playerHands.get(controllerId);
        int count = hand != null ? hand.size() : 0;
        accumulator.addPower(count);
        accumulator.addToughness(count);
    }
}
