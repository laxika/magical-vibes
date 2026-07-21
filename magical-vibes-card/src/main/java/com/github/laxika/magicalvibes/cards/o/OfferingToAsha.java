package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ARB", collectorNumber = "9")
public class OfferingToAsha extends Card {

    public OfferingToAsha() {
        // Counter target spell unless its controller pays {4}. You gain 4 life.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(4));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
