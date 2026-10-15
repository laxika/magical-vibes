package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "64")
public class StymiedHopes extends Card {

    public StymiedHopes() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));
        addEffect(EffectSlot.SPELL, new ScryEffect(1));
    }
}
