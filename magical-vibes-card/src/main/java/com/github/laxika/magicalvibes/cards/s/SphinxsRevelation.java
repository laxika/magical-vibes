package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "RTR", collectorNumber = "200")
public class SphinxsRevelation extends Card {

    public SphinxsRevelation() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
    }
}
