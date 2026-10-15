package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "32")
public class RitualOfRejuvenation extends Card {

    public RitualOfRejuvenation() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
