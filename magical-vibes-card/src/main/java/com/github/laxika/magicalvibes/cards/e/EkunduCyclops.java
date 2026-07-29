package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustAttackIfAnotherCreatureAttacksEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MIR", collectorNumber = "171")
public class EkunduCyclops extends Card {

    public EkunduCyclops() {
        addEffect(EffectSlot.STATIC, new MustAttackIfAnotherCreatureAttacksEffect());
    }
}
