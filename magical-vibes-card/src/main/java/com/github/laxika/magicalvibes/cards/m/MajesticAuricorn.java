package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "IKO", collectorNumber = "22")
public class MajesticAuricorn extends Card {

    public MajesticAuricorn() {
        addEffect(EffectSlot.ON_SELF_MUTATES, new GainLifeEffect(4));
    }
}
