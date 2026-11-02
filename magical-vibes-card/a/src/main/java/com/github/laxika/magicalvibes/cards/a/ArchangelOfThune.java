package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "5")
@CardRegistration(set = "IMA", collectorNumber = "8")
@CardRegistration(set = "SLD", collectorNumber = "1337")
@CardRegistration(set = "SLD", collectorNumber = "2430")
@CardRegistration(set = "2XM", collectorNumber = "5")
@CardRegistration(set = "MAR", collectorNumber = "41")
@CardRegistration(set = "PIO", collectorNumber = "4")
public class ArchangelOfThune extends Card {

    public ArchangelOfThune() {
        addTrigger(Triggers.youGainLife(), new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()));
    }
}
