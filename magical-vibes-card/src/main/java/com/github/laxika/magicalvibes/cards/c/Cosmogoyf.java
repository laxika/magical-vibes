package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInExile;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "EOE", collectorNumber = "215")
public class Cosmogoyf extends Card {

    public Cosmogoyf() {
        CardsInExile cardsOwnedInExile = new CardsInExile(null, CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                cardsOwnedInExile, new Sum(cardsOwnedInExile, new Fixed(1))));
    }
}
