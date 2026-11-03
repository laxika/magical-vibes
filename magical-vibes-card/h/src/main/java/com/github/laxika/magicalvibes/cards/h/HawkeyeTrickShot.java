package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "506")
public class HawkeyeTrickShot extends Card {

    public HawkeyeTrickShot() {
        // Whenever Hawkeye or another Hero you control enters, it deals damage equal to the
        // number of Heroes you control to any target.
        addTrigger(Triggers.enters(ObjectRelation.THIS), heroDamage());
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.HERO))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                heroDamage());
    }

    private static DealDamageToAnyTargetEffect heroDamage() {
        return new DealDamageToAnyTargetEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.HERO), CountScope.CONTROLLER));
    }
}
