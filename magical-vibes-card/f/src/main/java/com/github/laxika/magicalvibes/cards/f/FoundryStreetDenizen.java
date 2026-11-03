package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GTC", collectorNumber = "92")
@CardRegistration(set = "M15", collectorNumber = "141")
@CardRegistration(set = "DDT", collectorNumber = "38")
public class FoundryStreetDenizen extends Card {

    public FoundryStreetDenizen() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentColorInPredicate(Set.of(CardColor.RED)))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new BoostSelfEffect(1, 0));
    }
}
