package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;

@CardRegistration(set = "IKO", collectorNumber = "231")
public class UmoriTheCollector extends Card {

    public UmoriTheCollector() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardTypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardHasSourceChosenCardTypePredicate(), 1, CostModificationScope.SELF));
    }
}
