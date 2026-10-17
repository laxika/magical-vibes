package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MillAsEntersEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "DMU", collectorNumber = "98")
public class MonstrousWarLeech extends Card {

    public MonstrousWarLeech() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{U}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillAsEntersEffect(4, true));

        var greatestManaValue = new GreatestManaValueAmongCardsInGraveyard(
                new CardTruePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(greatestManaValue, greatestManaValue));
    }
}
