package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "USG", collectorNumber = "271")
public class Rejuvenate extends Card {

    public Rejuvenate() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(6));
        addCycling("{2}");
    }
}
