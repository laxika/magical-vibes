package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "83")
@CardRegistration(set = "LCC", collectorNumber = "189")
@CardRegistration(set = "VOC", collectorNumber = "125")
public class CordialVampire extends Card {

    public CordialVampire() {
        var vampires = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);
        var trigger = new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1, vampires);

        addTrigger(Triggers.creatureDies(ObjectRelation.THIS_OR_ANOTHER), trigger);
    }
}
