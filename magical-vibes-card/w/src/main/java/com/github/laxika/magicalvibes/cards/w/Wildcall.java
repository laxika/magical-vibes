package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardAndPutCountersEffect;

@CardRegistration(set = "FRF", collectorNumber = "146")
public class Wildcall extends Card {

    public Wildcall() {
        addEffect(EffectSlot.SPELL,
                new ManifestTopCardAndPutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
