package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestTopCardAndPutCountersEffect;

@CardRegistration(set = "FRF", collectorNumber = "129")
public class FormlessNurturing extends Card {

    public FormlessNurturing() {
        addEffect(EffectSlot.SPELL,
                new ManifestTopCardAndPutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
