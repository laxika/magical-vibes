package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AbstractPerformanceEffect;

@CardRegistration(set = "SOC", collectorNumber = "17")
@CardRegistration(set = "SOC", collectorNumber = "67")
public class AbstractPerformance extends Card {

    public AbstractPerformance() {
        addEffect(EffectSlot.SPELL, new AbstractPerformanceEffect());
    }
}
