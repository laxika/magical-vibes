package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOT", collectorNumber = "22")
@CardRegistration(set = "WOT", collectorNumber = "69")
@CardRegistration(set = "WOT", collectorNumber = "89")
@CardRegistration(set = "HBG", collectorNumber = "126")
@CardRegistration(set = "MSC", collectorNumber = "150")
@CardRegistration(set = "MSC", collectorNumber = "334")
@CardRegistration(set = "LCC", collectorNumber = "159")
@CardRegistration(set = "C17", collectorNumber = "11")
public class KindredDiscovery extends Card {

    public KindredDiscovery() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSourceChosenSubtypePredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new DrawCardEffect());
        addTrigger(Triggers.creatureYouControlAttacks().withFilter(new PermanentHasSourceChosenSubtypePredicate()),
                new DrawCardEffect());
    }
}
