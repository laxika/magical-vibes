package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "76")
public class ForceSpike extends Card {

    public ForceSpike() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));
    }
}
