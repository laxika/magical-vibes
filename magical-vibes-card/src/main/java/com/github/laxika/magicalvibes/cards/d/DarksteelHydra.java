package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "YONE", collectorNumber = "20")
public class DarksteelHydra extends Card {

    public DarksteelHydra() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.OIL, new XValue()));

        CountersOnSource oilCounters = new CountersOnSource(CounterType.OIL);
        Scaled twiceOilCounters = new Scaled(oilCounters, 2);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(twiceOilCounters, twiceOilCounters));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConjureCardToHandEffect("Darksteel Ingot"),
                new ConjureCardToHandEffect("Darksteel Plate")));
    }
}
