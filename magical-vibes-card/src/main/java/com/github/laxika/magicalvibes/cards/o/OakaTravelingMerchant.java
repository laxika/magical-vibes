package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "39")
@CardRegistration(set = "FIC", collectorNumber = "144")
public class OakaTravelingMerchant extends Card {

    public OakaTravelingMerchant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromControlledPermanentCost(
                                CounterType.ANY,
                                new PermanentNotPredicate(new PermanentIsLandPredicate())),
                        new DrawCardEffect()
                ),
                "{T}, Remove a counter from a nonland permanent you control: Draw a card."
        ));
    }
}
