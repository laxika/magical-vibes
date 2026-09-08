package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoblinGameEffect;

@CardRegistration(set = "ONS", collectorNumber = "219")
public class MenacingOgre extends Card {

    public MenacingOgre() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GoblinGameEffect(true, 2));
    }
}
