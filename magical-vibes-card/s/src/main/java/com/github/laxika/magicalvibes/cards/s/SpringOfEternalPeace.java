package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "PTK", collectorNumber = "148")
public class SpringOfEternalPeace extends Card {

    public SpringOfEternalPeace() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(8));
    }
}
