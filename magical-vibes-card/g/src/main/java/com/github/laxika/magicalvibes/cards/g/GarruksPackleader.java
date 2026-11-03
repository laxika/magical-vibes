package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "177")
@CardRegistration(set = "M13", collectorNumber = "175")
@CardRegistration(set = "BLC", collectorNumber = "218")
@CardRegistration(set = "C19", collectorNumber = "168")
@CardRegistration(set = "FDC", collectorNumber = "205")
public class GarruksPackleader extends Card {

    public GarruksPackleader() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentPowerAtLeastPredicate(3))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
