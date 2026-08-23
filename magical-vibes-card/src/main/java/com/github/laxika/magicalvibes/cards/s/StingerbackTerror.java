package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "147")
public class StingerbackTerror extends Card {

    public StingerbackTerror() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{R}"))));
        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        Scaled handSize = new Scaled(cardsInHand, -1);
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(handSize, handSize, GrantScope.SELF));
    }
}
