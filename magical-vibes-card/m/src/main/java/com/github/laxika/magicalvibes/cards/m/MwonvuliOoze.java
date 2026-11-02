package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "135")
public class MwonvuliOoze extends Card {

    public MwonvuliOoze() {
        // Cumulative upkeep {2}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{2}"));

        // Mwonvuli Ooze's power and toughness are each equal to 1 plus twice the number of age counters on it.
        DynamicAmount onePlusTwiceAgeCounters = new Sum(
                new Fixed(1),
                new Scaled(new CountersOnSource(CounterType.AGE), 2));
        addStaticEffect(new SetPowerToughnessToAmountEffect(onePlusTwiceAgeCounters, onePlusTwiceAgeCounters));
    }
}
