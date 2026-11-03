package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "174")
@CardRegistration(set = "TLE", collectorNumber = "40")
@CardRegistration(set = "TDC", collectorNumber = "254")
@CardRegistration(set = "C19", collectorNumber = "163")
@CardRegistration(set = "C17", collectorNumber = "148")
@CardRegistration(set = "SCD", collectorNumber = "180")
@CardRegistration(set = "FDC", collectorNumber = "201")
public class ElementalBond extends Card {

    public ElementalBond() {
        // Whenever a creature you control with power 3 or greater enters, draw a card.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentPowerAtLeastPredicate(3))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new DrawCardEffect(1));
    }
}
