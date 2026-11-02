package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "20")
public class DarksteelHydra extends Card {

    public DarksteelHydra() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.OIL, new XValue()));

        CountersOnSource oilCounters = new CountersOnSource(CounterType.OIL);
        Scaled twiceOilCounters = new Scaled(oilCounters, 2);
        addStaticEffect(new SetPowerToughnessToAmountEffect(twiceOilCounters, twiceOilCounters));

        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new ConjureCardToHandEffect("DST", "112"),
                new ConjureCardToHandEffect("MBS", "104")));
    }
}
