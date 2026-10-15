package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentCounterCountAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "257")
@CardRegistration(set = "LCI", collectorNumber = "388")
public class TheMillenniumCalendar extends Card {

    public TheMillenniumCalendar() {
        addEffect(EffectSlot.ON_CONTROLLER_UNTAPS_DURING_UNTAP_STEP,
                new PutCountersOnSelfEffect(CounterType.TIME, new EventValue()));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DoubleCountersOnSourceEffect(CounterType.TIME)),
                "{2}, {T}: Double the number of time counters on The Millennium Calendar."));
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                new PermanentCounterCountAtLeastPredicate(CounterType.TIME, 1000),
                List.of(
                        new SacrificeSelfEffect(),
                        new LoseLifeEffect(1000, LoseLifeRecipient.EACH_OPPONENT)),
                "The Millennium Calendar's state-triggered ability"));
    }
}
