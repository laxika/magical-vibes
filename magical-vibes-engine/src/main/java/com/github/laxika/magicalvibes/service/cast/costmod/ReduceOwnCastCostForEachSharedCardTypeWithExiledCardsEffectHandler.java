package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffectHandler
        implements CostModificationHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (source.sourcePermanent() == null || !source.controlledBy(context.castingPlayerId())) {
            return 0;
        }

        EnumSet<CardType> exiledCardTypes = EnumSet.noneOf(CardType.class);
        for (Card card : context.gameData().getCardsExiledByPermanent(source.sourcePermanent().getId())) {
            addCardTypes(exiledCardTypes, card);
        }

        EnumSet<CardType> sharedCardTypes = cardTypes(context.spell());
        sharedCardTypes.retainAll(exiledCardTypes);
        return -sharedCardTypes.size();
    }

    private EnumSet<CardType> cardTypes(Card card) {
        EnumSet<CardType> cardTypes = EnumSet.noneOf(CardType.class);
        addCardTypes(cardTypes, card);
        return cardTypes;
    }

    private void addCardTypes(EnumSet<CardType> cardTypes, Card card) {
        if (card.getType() != null) {
            cardTypes.add(card.getType());
        }
        cardTypes.addAll(card.getAdditionalTypes());
    }
}
