package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M10", collectorNumber = "2")
public class AngelsMercy extends Card {

    public AngelsMercy() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(7));
    }
}
