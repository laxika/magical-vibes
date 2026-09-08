package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "259")
public class Megatherium extends Card {

    public Megatherium() {
        // When this creature enters, sacrifice it unless you pay {1} for each card in your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ForcedCostOrElseEffect(
                        PayManaCost.withGenericIncrease("{0}", new CardsInHand(CountScope.CONTROLLER)),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
