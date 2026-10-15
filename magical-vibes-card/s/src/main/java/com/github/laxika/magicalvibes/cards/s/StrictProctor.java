package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StrictProctorEffect;

@CardRegistration(set = "STX", collectorNumber = "33")
public class StrictProctor extends Card {

    public StrictProctor() {
        addEffect(EffectSlot.STATIC, new StrictProctorEffect());
    }
}
