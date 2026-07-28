package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersInsteadOfUntappingEffect;

@CardRegistration(set = "ICE", collectorNumber = "241")
public class FreyalisesWinds extends Card {

    public FreyalisesWinds() {
        // Whenever a permanent becomes tapped, put a wind counter on it. "A permanent" means any
        // permanent, so both the ally- and opponent-scoped tap slots are used; both carry the
        // tapped permanent as the trigger's non-target reference.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED,
                new PutCounterOnTriggeringPermanentEffect(CounterType.WIND));
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED,
                new PutCounterOnTriggeringPermanentEffect(CounterType.WIND));

        // If a permanent with a wind counter on it would untap during its controller's untap step,
        // remove all wind counters from it instead.
        addEffect(EffectSlot.STATIC, new RemoveCountersInsteadOfUntappingEffect(CounterType.WIND));
    }
}
