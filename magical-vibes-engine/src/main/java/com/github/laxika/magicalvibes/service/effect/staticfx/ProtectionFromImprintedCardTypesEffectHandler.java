package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromImprintedCardTypesEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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
        // Every card this permanent exiled still counts (several imprint triggers can each exile one).
        List<Card> imprintedCards = new ArrayList<>(context.gameData().getCardsExiledByPermanent(context.sourceId()));
        Card imprinted = context.gameData().getImprintedCard(context.sourceCard());
        if (imprinted != null && imprintedCards.stream().noneMatch(card -> card.getId().equals(imprinted.getId()))
                && context.gameData().findExiledCard(imprinted.getId()) != null) {
            imprintedCards.add(imprinted);
        }
        if (imprintedCards.isEmpty()) {
            return;
        }

        EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
        for (Card card : imprintedCards) {
            cardTypes.add(card.getType());
            cardTypes.addAll(card.getAdditionalTypes());
        }
        accumulator.addGrantedEffect(new ProtectionFromCardTypesEffect(cardTypes));
    }
}
