package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PopulateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "REX", collectorNumber = "5")
@CardRegistration(set = "REX", collectorNumber = "31")
public class LifeFindsAWay extends Card {

    public LifeFindsAWay() {
        // Whenever a nontoken creature you control with power 4 or greater enters, populate.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                                        new PermanentPowerAtLeastPredicate(4))))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new PopulateEffect());
    }
}
