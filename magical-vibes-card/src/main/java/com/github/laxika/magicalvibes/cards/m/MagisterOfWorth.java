package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GraceOrCondemnationEffect;

@CardRegistration(set = "VMA", collectorNumber = "255")
public class MagisterOfWorth extends Card {

    public MagisterOfWorth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GraceOrCondemnationEffect());
    }
}
