package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReduceMinusOneMinusOneCountersEffect;

@CardRegistration(set = "AKH", collectorNumber = "38")
public class VizierOfRemedies extends Card {

    public VizierOfRemedies() {
        // If one or more -1/-1 counters would be put on a creature you control, that many -1/-1
        // counters minus one are put on it instead.
        addEffect(EffectSlot.STATIC, new ReduceMinusOneMinusOneCountersEffect());
    }
}
