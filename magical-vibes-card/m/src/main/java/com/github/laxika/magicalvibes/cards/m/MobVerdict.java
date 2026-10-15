package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MobVerdictEffect;

@CardRegistration(set = "MKC", collectorNumber = "33")
@CardRegistration(set = "MKC", collectorNumber = "343")
public class MobVerdict extends Card {

    public MobVerdict() {
        addEffect(EffectSlot.SPELL, new MobVerdictEffect());
    }
}
