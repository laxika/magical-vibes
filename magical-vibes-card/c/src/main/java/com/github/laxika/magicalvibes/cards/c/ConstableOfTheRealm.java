package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "10")
@CardRegistration(set = "MOC", collectorNumber = "179")
public class ConstableOfTheRealm extends Card {

    public ConstableOfTheRealm() {
        // Renown 2
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(2));

        // Whenever one or more +1/+1 counters are put on this creature, exile up to one other
        // target nonland permanent until this creature leaves the battlefield.
        PermanentPredicate otherNonlandPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                otherNonlandPermanent,
                "Target must be another nonland permanent"
        ), 0, 1).addTrigger(Triggers.plusOneCountersPutOnThis(),
                new ExileTargetPermanentUntilSourceLeavesEffect(false, otherNonlandPermanent));
    }
}
