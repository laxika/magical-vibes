package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FlurryCopyOrDrawTriggerEffect;

@CardRegistration(set = "TDC", collectorNumber = "7")
public class ShikoAndNarsetUnified extends Card {

    public ShikoAndNarsetUnified() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new FlurryCopyOrDrawTriggerEffect());
    }
}
