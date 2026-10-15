package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "WTH", collectorNumber = "135")
public class MwonvuliOoze extends Card {

    public MwonvuliOoze() {
        // Cumulative upkeep {2}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{2}"));

        // Mwonvuli Ooze's power and toughness are each equal to 1 plus twice the number of age counters on it.
        DynamicAmount onePlusTwiceAgeCounters = new Sum(
                new Fixed(1),
                new Scaled(new CountersOnSource(CounterType.AGE), 2));
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(onePlusTwiceAgeCounters, onePlusTwiceAgeCounters));
    }
}
