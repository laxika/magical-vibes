package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersInsteadOfUntappingEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "241")
@CardRegistration(set = "ME3", collectorNumber = "119")
public class FreyalisesWinds extends Card {

    public FreyalisesWinds() {
        // Whenever a permanent becomes tapped, put a wind counter on it. "A permanent" means any
        // permanent, so both the ally- and opponent-scoped tap slots are used; both carry the
        // tapped permanent as the trigger's non-target reference.
        addTrigger(Triggers.permanentYouControlBecomesTapped(),
                new PutCounterOnTargetPermanentEffect(Subjects.triggering(), CounterType.WIND));
        addTrigger(Triggers.permanentOpponentControlsBecomesTapped(),
                new PutCounterOnTargetPermanentEffect(Subjects.triggering(), CounterType.WIND));

        // If a permanent with a wind counter on it would untap during its controller's untap step,
        // remove all wind counters from it instead.
        addStaticEffect(new RemoveCountersInsteadOfUntappingEffect(CounterType.WIND));
    }
}
