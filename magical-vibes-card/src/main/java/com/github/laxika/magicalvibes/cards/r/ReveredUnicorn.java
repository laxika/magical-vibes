package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "WTH", collectorNumber = "23")
public class ReveredUnicorn extends Card {

    public ReveredUnicorn() {
        // Cumulative upkeep {1}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));
        // When this creature leaves the battlefield, you gain life equal to the number of age
        // counters on it. The count is frozen from the leaving permanent by the self-leaves
        // collector, since the source is gone by resolution time.
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new GainLifeEffect(new CountersOnSource(CounterType.AGE)));
    }
}
