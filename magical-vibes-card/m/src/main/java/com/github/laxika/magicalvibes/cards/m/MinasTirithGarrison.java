package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.TapAnyNumberOfPermanentsThenDrawPerTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "180")
public class MinasTirithGarrison extends Card {

    public MinasTirithGarrison() {
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                new CardsInHand(CountScope.CONTROLLER), new Fixed(5)));

        addTrigger(Triggers.attacks(), new TapAnyNumberOfPermanentsThenDrawPerTappedEffect(
                new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    }
}
