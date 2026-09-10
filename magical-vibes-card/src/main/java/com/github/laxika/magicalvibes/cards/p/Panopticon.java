package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "MOC", collectorNumber = "153")
@CardRegistration(set = "OHOP", collectorNumber = "29")
public class Panopticon extends Card {
    public Panopticon() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, new DrawCardEffect(1));
        addEffect(EffectSlot.DRAW_TRIGGERED, new DrawCardEffect(1));
        addEffect(EffectSlot.CHAOS_TRIGGERED, new DrawCardEffect(1));
    }
}
