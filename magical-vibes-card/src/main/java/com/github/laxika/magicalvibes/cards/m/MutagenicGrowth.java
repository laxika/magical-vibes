package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "NPH", collectorNumber = "116")
public class MutagenicGrowth extends Card {

    public MutagenicGrowth() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
