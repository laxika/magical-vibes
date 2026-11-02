package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "41")
@CardRegistration(set = "DDQ", collectorNumber = "22")
@CardRegistration(set = "MOC", collectorNumber = "216")
@CardRegistration(set = "LTC", collectorNumber = "181")
public class VillageBellRinger extends Card {

    public VillageBellRinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsCreaturePredicate()));
    }
}
