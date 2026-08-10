package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromImprintedCardTypesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class ProtectionFromImprintedCardTypesEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromImprintedCardTypesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Card imprinted = context.gameData().getImprintedCard(context.source().getCard());
        if (imprinted == null || context.gameData().findExiledCard(imprinted.getId()) == null) {
            return;
        }

        EnumSet<CardType> cardTypes = EnumSet.of(imprinted.getType());
        cardTypes.addAll(imprinted.getAdditionalTypes());
        accumulator.addGrantedEffect(new ProtectionFromCardTypesEffect(cardTypes));
    }
}
