package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "71")
@CardRegistration(set = "E02", collectorNumber = "20")
@CardRegistration(set = "C21", collectorNumber = "147")
@CardRegistration(set = "VOC", collectorNumber = "132")
public class NecropolisRegent extends Card {

    public NecropolisRegent() {
        // Whenever a creature you control deals combat damage to a player, put that many
        // +1/+1 counters on it: the damage is the entry's event value, "it" its triggering permanent.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
