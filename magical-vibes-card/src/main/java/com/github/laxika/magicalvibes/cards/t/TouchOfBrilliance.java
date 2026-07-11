package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "76")
@CardRegistration(set = "P02", collectorNumber = "58")
public class TouchOfBrilliance extends Card {

    public TouchOfBrilliance() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
