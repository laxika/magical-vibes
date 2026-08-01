package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TimeAndTideEffect;

@CardRegistration(set = "VIS", collectorNumber = "46")
public class TimeAndTide extends Card {

    public TimeAndTide() {
        // Simultaneously, all phased-out creatures phase in and all creatures with phasing phase out.
        addEffect(EffectSlot.SPELL, new TimeAndTideEffect());
    }
}
