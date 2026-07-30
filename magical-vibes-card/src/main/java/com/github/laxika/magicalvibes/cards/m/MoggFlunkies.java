package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;

@CardRegistration(set = "M13", collectorNumber = "143")
public class MoggFlunkies extends Card {

    public MoggFlunkies() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockAloneEffect());
    }
}
