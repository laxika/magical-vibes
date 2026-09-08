package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "160")
public class CloudKey extends Card {

    public CloudKey() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardTypeOnEnterEffect(List.of(
                CardType.LAND,
                CardType.PLANESWALKER,
                CardType.BATTLE,
                CardType.KINDRED
        )));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardHasSourceChosenCardTypePredicate(), 1, CostModificationScope.SELF));
    }
}
