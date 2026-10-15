package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BendOrBreakEffect;

@CardRegistration(set = "INV", collectorNumber = "137")
public class BendOrBreak extends Card {

    public BendOrBreak() {
        addEffect(EffectSlot.SPELL, new BendOrBreakEffect());
    }
}
