package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasSourceChosenCardTypePredicate;

@CardRegistration(set = "IKO", collectorNumber = "231")
@CardRegistration(set = "MUL", collectorNumber = "62")
@CardRegistration(set = "MUL", collectorNumber = "127")
@CardRegistration(set = "MUL", collectorNumber = "192")
public class UmoriTheCollector extends Card {

    public UmoriTheCollector() {
        addStaticEffect(new ChooseCardTypeOnEnterEffect());
        addStaticEffect(new ReduceCastCostForMatchingSpellsEffect(
                new CardHasSourceChosenCardTypePredicate(), 1, CostModificationScope.SELF));
    }
}
